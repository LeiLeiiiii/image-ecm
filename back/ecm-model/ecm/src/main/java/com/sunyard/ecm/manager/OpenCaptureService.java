package com.sunyard.ecm.manager;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmBaseInfoDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.es.EsEcmFileMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.ecm.po.EcmFileHistory;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.vo.EcmDelVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author WJJ
 * @since 2025-05-15
 * @desc 影像采集业务公共类
 */
@Service
public class OpenCaptureService {
    @Value("${fileIndex:ecm_file_dev}")
    private String fileIndex;
    @Resource
    protected HttpServletResponse response;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;
    @Resource
    private EsEcmFileMapper esEcmFileMapper;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private CommonService commonService;
    @Resource
    private CaptureSubmitService captureSubmitService;


    /**
     * 根据业务或者资料删除影像文件
     */
    @Transactional(rollbackFor = Exception.class)
    public Result deleteFileByBusiOrDoc(EcmDelVO vo) {
        //校验用户信息
        AccountTokenExtendDTO accountTokenExtendDTO = busiCacheService.checkUser(vo.getEcmBaseInfoDTO(), null);
        EcmBaseInfoDTO ecmBaseInfoDTO = vo.getEcmBaseInfoDTO();
        ecmBaseInfoDTO.setUserName(accountTokenExtendDTO.getName());
        ecmBaseInfoDTO.setOrgName(accountTokenExtendDTO.getOrgName());
        vo.setEcmBaseInfoDTO(ecmBaseInfoDTO);
        AssertUtils.isNull(vo.getAppCode(), "业务分类不存在！");
        AssertUtils.isNull(vo.getBusiNo(), "业务主索引不存在！");
        //校验传入参数时候合规
        String appNo = vo.getAppCode();
        String busiNo = vo.getBusiNo();
        String appCode = null;
        List<String> docNoList = vo.getDocNo();
        List<Long> fileIdList = vo.getFileIdList();
        Long appTypeId = null;
        Long busiId = null;
        //        List<Long> docIds = new ArrayList<>();
        /*
          删除方式有三种
          1.传入 业务类型 + 业务主索引： 删除指定业务下所有文件
          2.传入 业务类型 + 业务主索引 + 资料节点 ： 删除指定业务下指定资料节点的所有文件
          3.传入 业务类型 + 业务主索引 + 文件IDs ： 删除指定业务下的指定文件
         */
        if (StrUtil.isNotBlank(appNo) && StrUtil.isNotBlank(busiNo)) {
            //获取业务类型
            LambdaQueryWrapper<EcmAppDef> appWrapper = new LambdaQueryWrapper<>();
            appWrapper.eq(EcmAppDef::getAppCode, appNo);
            EcmAppDef ecmAppDef = ecmAppDefMapper.selectOne(appWrapper);
            if (ecmAppDef != null) {
                appCode = ecmAppDef.getAppCode();
                LambdaQueryWrapper<EcmBusiInfo> infoWrapper = new LambdaQueryWrapper<>();
                infoWrapper.eq(EcmBusiInfo::getAppCode, appCode);
                infoWrapper.eq(EcmBusiInfo::getBusiNo, busiNo);
                EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(infoWrapper);
                if (ObjectUtils.isEmpty(ecmBusiInfo)) {
                    return Result.error("不存在此业务数据", IcmsConstants.DATA_FAILED);
                }
                busiId = ecmBusiInfo.getBusiId();
            } else {
                return Result.error("不存在此业务分类", IcmsConstants.DATA_FAILED);
            }
            if (CollectionUtils.isEmpty(docNoList) && CollectionUtils.isEmpty(fileIdList)) {
                //删除业务下所有文件
                //获取指定业务下的文件ID
                List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(busiId);
                List<Long> fileIds = new ArrayList<>();
                if(!CollectionUtils.isEmpty(fileInfos)){
                    List<FileInfoRedisDTO> ecmFileInfos = fileInfos.stream().filter(s -> StateConstants.NO.equals(s.getState())).collect(Collectors.toList());
                    ecmFileInfos.forEach(e -> fileIds.add(e.getFileId()));
                }
                vo.setFileIdList(fileIds);
                //删除文件
                LambdaUpdateWrapper<EcmFileInfo> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(EcmFileInfo::getBusiId, busiId);
                updateWrapper.set(EcmFileInfo::getState, IcmsConstants.ONE);
                ecmFileInfoMapper.update(null, updateWrapper);
                //更新缓存数据
                deleteFileInfoByMoreToRedis(vo, busiId);
                //更新es数据
                for (Long fileId : vo.getFileIdList()) {
                    deleteFileInfoByMoreToEs(fileId);
                }
            } else if (!CollectionUtils.isEmpty(fileIdList)) {
                //若指定文件中有不存在的文件，则阻断流程
                for (Long fileId : fileIdList) {
                    FileInfoRedisDTO fileInfoRedisSingle = busiCacheService
                            .getFileInfoRedisSingle(busiId, fileId);
                    AssertUtils.isTrue(fileInfoRedisSingle==null, "文件" + fileId + "不存在");
                }
                //删除指定文件
                LambdaUpdateWrapper<EcmFileInfo> fileWrapper = new LambdaUpdateWrapper<>();
                fileWrapper.eq(EcmFileInfo::getBusiId, busiId);
                fileWrapper.in(EcmFileInfo::getFileId, vo.getFileIdList());
                fileWrapper.set(EcmFileInfo::getState, IcmsConstants.ONE);
                ecmFileInfoMapper.update(null, fileWrapper);
                //更新缓存数据
                deleteFileInfoByMoreToRedis(vo, busiId);
                //更新es数据
                for (Long fileId : vo.getFileIdList()) {
                    deleteFileInfoByMoreToEs(fileId);
                }
            } else if (!CollectionUtils.isEmpty(docNoList)) {
                //查询业务关联资料
                String busiNo1 = vo.getBusiNo();
                String appNo1 = vo.getAppCode();
                LambdaQueryWrapper<EcmBusiInfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(EcmBusiInfo::getBusiNo, busiNo1);
                wrapper.eq(EcmBusiInfo::getAppCode, appNo1);
                EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectOne(wrapper);
                if (ecmBusiInfo != null) {
                    Integer rightVer = ecmBusiInfo.getRightVer();
                    //根据权限版本查询资料
                    LambdaQueryWrapper<EcmDocDefRelVer> wrapper1 = new LambdaQueryWrapper<>();
                    wrapper1.eq(EcmDocDefRelVer::getRightVer, rightVer);
                    wrapper1.eq(EcmDocDefRelVer::getAppCode, appNo1);
                    //若指定文件中有不存在的节点，则阻断流程
                    for (String docCode : docNoList) {
                        wrapper1.eq(EcmDocDefRelVer::getDocCode, docCode);
                        EcmDocDefRelVer ecmDocDefRelVer = ecmDocDefRelVerMapper.selectOne(wrapper1);
                        AssertUtils.isNull(ecmDocDefRelVer, "此业务版本未关联资料" + docCode);
                    }
                }
                //获取指定资料节点下的文件ID
                LambdaQueryWrapper<EcmFileInfo> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(EcmFileInfo::getBusiId, busiId);
                queryWrapper.eq(EcmFileInfo::getState, StateConstants.NO);
                queryWrapper.in(EcmFileInfo::getDocCode, docNoList);
                List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper.selectList(queryWrapper);
                List<Long> fileIds = new ArrayList<>();
                ecmFileInfos.forEach(e -> fileIds.add(e.getFileId()));
                vo.setFileIdList(fileIds);

                //删除指定资料节点下的文件
                LambdaUpdateWrapper<EcmFileInfo> fileWrapper = new LambdaUpdateWrapper<>();
                fileWrapper.eq(EcmFileInfo::getBusiId, busiId);
                fileWrapper.in(EcmFileInfo::getDocCode, docNoList);
                fileWrapper.set(EcmFileInfo::getState, StateConstants.YES);
                ecmFileInfoMapper.update(null, fileWrapper);

                //更新缓存数据
                deleteFileInfoByMoreToRedis(vo, busiId);
                //更新es数据
                for (Long fileId : vo.getFileIdList()) {
                    deleteFileInfoByMoreToEs(fileId);
                }
            }
        } else {
            return Result.error("数据有误", IcmsConstants.DATA_FAILED);
        }
        return Result.success(true);
    }

    /**
     * 删除文件缓存信息
     */
    private void deleteFileInfoByMoreToRedis(EcmDelVO vo, Long busiId) {
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(busiId, vo.getFileIdList());
        fileInfoRedis.stream().filter(p -> StateConstants.NO.equals(p.getState()) && vo.getFileIdList().contains(p.getFileId())).forEach(p -> {
            //已删除状态
            p.setState(IcmsConstants.ONE);
            p.setUpdateUser(vo.getEcmBaseInfoDTO().getUserCode());
            p.setUpdateTime(new Date());
        });
        //添加文件历史记录
        addFileHistory(fileInfoRedis, vo.getEcmBaseInfoDTO().getUserCode(), IcmsConstants.DELETE_FILE_STRING);
        busiCacheService.updateFileInfoRedis(fileInfoRedis);
    }

    /**
     * 删除es数据
     */
    private void deleteFileInfoByMoreToEs(Long fileId) {
        esEcmFileMapper.deleteById(fileId + "", fileIndex);
    }

    /**
     * 添加删除文件操作记录
     */
    private void addFileHistory(List<FileInfoRedisDTO> fileInfoRedisEntities, String curentUserId, String deleteFile) {
        if (CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            return;
        }
        for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedisEntities) {
            if (IcmsConstants.ONE.equals(fileInfoRedisDTO.getState())) {
                String ext = getExt(fileInfoRedisDTO);
                //新增一条文件历史记录
                EcmFileHistory ecmFileHistory = commonService.insertFileHistory(fileInfoRedisDTO.getBusiId(), fileInfoRedisDTO.getFileId(),
                        fileInfoRedisDTO.getNewFileId(), deleteFile, curentUserId, fileInfoRedisDTO.getSize(),ext);
                if (CollectionUtils.isEmpty(fileInfoRedisDTO.getFileHistories())) {
                    List<EcmFileHistory> ecmFileHistories = new ArrayList<>();
                    ecmFileHistories.add(ecmFileHistory);
                    fileInfoRedisDTO.setFileHistories(ecmFileHistories);
                } else {
                    List<EcmFileHistory> fileHistories = fileInfoRedisDTO.getFileHistories();
                    ArrayList<EcmFileHistory> ecmFileHistories = new ArrayList<>(fileHistories);
                    ecmFileHistories.add(ecmFileHistory);
                    fileInfoRedisDTO.setFileHistories(ecmFileHistories);
                }
            }
        }
    }

    private String getExt(FileInfoRedisDTO fileInfoRedisDTO) {
        if(fileInfoRedisDTO.getNewFileName()!=null) {
            int lastIndex = fileInfoRedisDTO.getNewFileName().lastIndexOf('.');
            // 提取文件扩展名
            return fileInfoRedisDTO.getNewFileName().substring(lastIndex + 1);
        }else return null;
    }

}
