package com.sunyard.ecm.manager;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.sunyard.ecm.constant.DocRightConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.EcmBusExtendDTO;
import com.sunyard.ecm.dto.EcmUserDTO;
import com.sunyard.ecm.dto.ecm.EcmDocrightDefDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.dto.redis.UserBusiRedisDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.mapper.EcmAppAttrMapper;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAsyncTaskMapper;
import com.sunyard.ecm.mapper.EcmBusiDocMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiMetadataMapper;
import com.sunyard.ecm.mapper.EcmBusiVersionMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmDocDefRelVerMapper;
import com.sunyard.ecm.mapper.EcmFileCommentMapper;
import com.sunyard.ecm.mapper.EcmFileExpireInfoMapper;
import com.sunyard.ecm.mapper.EcmFileHistoryMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.EcmFileLabelMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.po.EcmBusiDoc;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiMetadata;
import com.sunyard.ecm.po.EcmBusiVersion;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import com.sunyard.ecm.po.EcmFileComment;
import com.sunyard.ecm.po.EcmFileExpireInfo;
import com.sunyard.ecm.po.EcmFileHistory;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.po.EcmFileLabel;
import com.sunyard.ecm.service.SysStrategyService;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.system.api.MenuApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lw
 * @describe 采集页面业务处理实现类
 * @since 2023-7-31
 */
@Slf4j
@Service
public class BusiCacheService {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private EcmFileLabelMapper ecmFileLabelMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmBusiVersionMapper ecmBusiVersionMapper;
    @Resource
    private EcmFileCommentMapper ecmFileCommentMapper;
    @Resource
    private EcmFileExpireInfoMapper ecmFileExpireInfoMapper;
    @Resource
    private EcmFileHistoryMapper ecmFileHistoryMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmAppAttrMapper ecmAppAttrMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;
    @Resource
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Resource
    private EcmDocDefRelVerMapper ecmDocDefRelVerMapper;
    @Resource
    private EcmAsyncTaskMapper asyncTaskMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private UserApi userApi;
    @Resource
    private MenuApi menuApi;
    @Resource
    private SysStrategyService sysStrategyService;
    @Resource
    private StaticTreePermissService staticTreePermissService;
    @Resource
    private CommonService commonService;

    /**
     * 查询数据库的值
     */
    public EcmBusiInfoRedisDTO followPerDb(Long busiId, AccountTokenExtendDTO currentUserId, Map<Long, String> fileIdDocFileSortMap) {
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(busiId);
        if (ObjectUtils.isEmpty(ecmBusiInfo)) {
            return null;
        }
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = new EcmBusiInfoRedisDTO();
        BeanUtils.copyProperties(ecmBusiInfo, ecmBusiInfoRedisDTO);
        //添加设备ID
        addEquipmentId(ecmBusiInfo, ecmBusiInfoRedisDTO);
        //添加业务轨迹
        addBusiTrajectoryToRedis(ecmBusiInfoRedisDTO);
        //添加业务属性
        addBusiAttrToRedis(ecmBusiInfoRedisDTO);
        //添加资料类型树，从业务目录树表获取
        addDocTypeTreeToEcmBusiDoc(ecmBusiInfoRedisDTO);
        //添加标记节点
        addEcmBusiDocs(ecmBusiInfoRedisDTO);
        //添加影像文件
        addFileInfoToRedis(ecmBusiInfoRedisDTO, fileIdDocFileSortMap);
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO1 = addBusiExtendInfoToRedis(ecmBusiInfoRedisDTO, currentUserId, null);
        return ecmBusiInfoRedisDTO1;
    }


    /**
     * redis添加业务相关信息缓存
     */
    public EcmBusiInfoRedisDTO addBusiExtendInfoToRedis(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO tokenExtend, EcmBusExtendDTO busExtendDTO) {
        EcmAppDef ecmAppDef = getRedisZip(ecmBusiInfoExtend.getAppCode());
        if (!ObjectUtils.isEmpty(ecmAppDef)) {
            //添加业务类型名称
            ecmBusiInfoExtend.setAppTypeName(ecmAppDef.getAppName());
            //添加设备id
            ecmBusiInfoExtend.setEquipmentId(ecmAppDef.getEquipmentId());
            //添加是否压缩
            ecmBusiInfoExtend.setIsQulity(ObjectUtils.isEmpty(ecmAppDef.getIsResize()) ? IcmsConstants.ZERO : ecmAppDef.getIsResize());
            //添加压缩比例
            ecmBusiInfoExtend.setResiz(ObjectUtils.isEmpty(ecmAppDef.getResize()) ? IcmsConstants.EIGHT_HUNDRED : ecmAppDef.getResize());
            //添加压缩质量
            ecmBusiInfoExtend.setQulity(ObjectUtils.isEmpty(ecmAppDef.getQulity()) ? IcmsConstants.ZERO_POINT_FOVE : ecmAppDef.getQulity());
            //添加创建人id
            ecmBusiInfoExtend.setCreateUser(ecmBusiInfoExtend.getCreateUser());
            //添加创建时间
            ecmBusiInfoExtend.setCreateTime(ecmBusiInfoExtend.getCreateTime());
            //添加设备id
            ecmBusiInfoExtend.setEquipmentId(ecmBusiInfoExtend.getEquipmentId());
            if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoExtend.getTreeType())) {
                //添加EcmBusiDocRedisDTO 资料信息
                List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = commonService.getDocList1(ecmBusiInfoExtend.getAppCode(), ecmBusiInfoExtend.getRightVer());
                ecmBusiInfoExtend.setEcmBusiDocRedisDTOS(ecmBusiDocRedisDTOS);
            }
        }
        //添加资料权限列表
//        addDocRightList(ecmBusiInfoExtend, tokenExtend, busExtendDTO);
        saveAndUpate(ecmBusiInfoExtend);
        return ecmBusiInfoExtend;
    }


    /**
     * 获取动态树叶子节点CODE
     */
    private void getLeafCode(List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS, List<String> docCodes) {
        if (CollectionUtils.isEmpty(ecmBusiDocRedisDTOS)) {
            return;
        }
        ecmBusiDocRedisDTOS.forEach(e -> {
            if (!CollectionUtils.isEmpty(e.getChildren())) {
                List<EcmBusiDocRedisDTO> children = e.getChildren();
                getLeafCode(children, docCodes);
            } else {
                String docCode = e.getDocCode();
                docCodes.add(docCode);
            }
        });
    }


    /**
     * 动态树
     */
    public List<EcmDocrightDefDTO> vTreeLogic(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO tokenExtend) {
        ArrayList<EcmDocrightDefDTO> docRightList = new ArrayList<>();
        List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = ecmBusiInfoExtend.getEcmBusiDocRedisDTOS();
        List<String> docCodes = new ArrayList<>();
        getLeafCode(ecmBusiDocRedisDTOS, docCodes);
        if (tokenExtend.isOut()) {
            docCodes.forEach(d -> {
                EcmDocrightDefDTO docrightDefDTO = new EcmDocrightDefDTO();
                docrightDefDTO.setReadRight(IcmsConstants.ZERO.toString());
                docrightDefDTO.setAddRight(IcmsConstants.ZERO.toString());
                docrightDefDTO.setThumRight(IcmsConstants.ZERO.toString());
                docrightDefDTO.setUpdateRight(IcmsConstants.ZERO.toString());
                docrightDefDTO.setDeleteRight(IcmsConstants.ZERO.toString());
                docrightDefDTO.setDownloadRight(IcmsConstants.ZERO.toString());
                docrightDefDTO.setPrintRight(IcmsConstants.ZERO.toString());
                docrightDefDTO.setOtherUpdate(IcmsConstants.ZERO.toString());
                docrightDefDTO.setMaxLen(DocRightConstants.ONE_THOUSAND);
                docrightDefDTO.setMinLen(DocRightConstants.ZERO);
                docrightDefDTO.setDocCode(d);
                docrightDefDTO.setAppCode(ecmBusiInfoExtend.getAppCode());
                docRightList.add(docrightDefDTO);
            });
        } else {
            //pc端动态树默认为全权限
            docCodes.forEach(d -> {
                EcmDocrightDefDTO docrightDefDTO = new EcmDocrightDefDTO();
                docrightDefDTO.setReadRight(IcmsConstants.ONE.toString());
                docrightDefDTO.setAddRight(IcmsConstants.ONE.toString());
                docrightDefDTO.setThumRight(IcmsConstants.ONE.toString());
                docrightDefDTO.setUpdateRight(IcmsConstants.ONE.toString());
                docrightDefDTO.setDeleteRight(IcmsConstants.ONE.toString());
                docrightDefDTO.setDownloadRight(IcmsConstants.ONE.toString());
                docrightDefDTO.setPrintRight(IcmsConstants.ONE.toString());
                docrightDefDTO.setOtherUpdate(IcmsConstants.ONE.toString());
                docrightDefDTO.setMaxLen(DocRightConstants.ONE_THOUSAND);
                docrightDefDTO.setMinLen(DocRightConstants.ZERO);
                docrightDefDTO.setEnableLenLimit(IcmsConstants.ONE.toString());
                docrightDefDTO.setDocCode(d);
                docrightDefDTO.setAppCode(ecmBusiInfoExtend.getAppCode());
                docRightList.add(docrightDefDTO);
            });
        }

        return docRightList;
    }

    /**
     * 当前登陆用户是否拥有动态树的菜单权限
     */
    public Boolean getVtreeFromPcByRole(Long userId, Integer isShow) {
        String vtreeAuth = null;
        String vtreeAuthC;
        String imageCapture = RoleConstants.IMAGE_CAPTURE;
        if (IcmsConstants.CAPTURE_PAGE.equals(isShow)) {
            vtreeAuth = RoleConstants.VTREE_AUTH;
        } else {
            vtreeAuth = RoleConstants.VTREE_AUTHSHOWC;
        }
        if (isShow == null) {
            //查看子菜单
            vtreeAuth = RoleConstants.VTREE_AUTHSHOW;
            imageCapture = RoleConstants.IMAGE_VIEW;
        }

        Boolean flag = false;
        Result<List<HashMap<String, String>>> vtreeAuth1 = menuApi.getRightButtonListByMenuPerms(userId, imageCapture);
        List<HashMap<String, String>> data = vtreeAuth1.getData();
        for (HashMap<String, String> datum : data) {
            if (datum.containsValue(vtreeAuth)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

//    private void addDocRightList(EcmBusiInfoRedisDTO ecmBusiInfoExtend, AccountTokenExtendDTO tokenExtend, EcmBusExtendDTO busExtendDTO) {
//        List<EcmDocrightDefDTO> docRightList = new ArrayList<>();
//        Integer treeType = ecmBusiInfoExtend.getTreeType();
//        if (IcmsConstants.DYNAMIC_TREE.equals(treeType)) {
//            List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS = ecmBusiInfoExtend.getEcmBusiDocRedisDTOS();
//            setNodeType(ecmBusiDocRedisDTOS);
//            //动态树权限
//            docRightList = vTreeLogic(ecmBusiInfoExtend, tokenExtend);
//        } else {
//            //静态树角色维度
//            if (busExtendDTO != null) {
//                //多维度
//                docRightList = commonService.dealRuleData(busExtendDTO, tokenExtend, ecmBusiInfoExtend.getRightVer());
//            } else {
//                docRightList = staticTreePermissService.roleDimLogic2(ecmBusiInfoExtend.getAppCode(), ecmBusiInfoExtend.getRightVer(), tokenExtend);
//            }
//        }
//        ecmBusiInfoExtend.setDocRightList(docRightList);
//    }

    private void setNodeType(List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS) {
        if (CollectionUtils.isEmpty(ecmBusiDocRedisDTOS)) {
            return;
        }
        for (EcmBusiDocRedisDTO ecmBusiDocRedisDTO : ecmBusiDocRedisDTOS) {
            if (CollectionUtils.isEmpty(ecmBusiDocRedisDTO.getChildren())) {
                ecmBusiDocRedisDTO.setNodeType(StateConstants.COMMON_ONE);
            } else {
                ecmBusiDocRedisDTO.setNodeType(StateConstants.ZERO);
                setNodeType(ecmBusiDocRedisDTO.getChildren());
            }
        }
    }

    private void addFileInfoToRedis(EcmBusiInfoRedisDTO ecmBusiInfoExtend, Map<Long, String> fileIdDocFileSortMap) {
        List<String> doc = new ArrayList<>();
        if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoExtend.getTreeType())) {

        } else {
            //获取所有docCode
            getDocCodeByReidsTreeDto(ecmBusiInfoExtend.getEcmBusiDocRedisDTOS(), doc);
        }

        getFileInfoRedis(ecmBusiInfoExtend, fileIdDocFileSortMap);
    }

    private List<FileInfoRedisDTO> getFileInfoRedis(EcmBusiInfoRedisDTO ecmBusiInfoExtend, Map<Long, String> fileIdDocFileSortMap) {
        //获取所有文件
        // 【关键修改】
        // 1. 显式启动分页，但设置为“查全部” (pageNum=0, pageSize=0)
        // 这会告诉 PageHelper: "不要修改我的 SQL，不要加 limit/rownum，直接执行原样"
        PageHelper.startPage(0, 0);
        List<EcmFileInfo> fileInfoList = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>()
                .eq(EcmFileInfo::getBusiId, ecmBusiInfoExtend.getBusiId()));
        if (!CollectionUtils.isEmpty(fileInfoList)) {
            List<EcmFileInfoDTO> fileInfoExtends = PageCopyListUtils.copyListProperties(fileInfoList, EcmFileInfoDTO.class);
            //获取存储服务中文件的基本信息并添加到影像文件信息对象中
            getStorageFileInfo(fileInfoExtends);
            List<FileInfoRedisDTO> fileInfoRedisEntities = PageCopyListUtils.copyListProperties(fileInfoExtends, FileInfoRedisDTO.class);
            //添加文件历史、 添加业务数据：appCode、appTypeName、busiNo
            addFileHistory(fileInfoRedisEntities, ecmBusiInfoExtend, fileIdDocFileSortMap, ecmBusiInfoExtend.getBusiId());
            //添加文件批注数量
            addFileCommentCount(fileInfoRedisEntities);
            //添加文件標簽
            addFileLabel(ecmBusiInfoExtend.getBusiId(), fileInfoRedisEntities);
            //添加文件期限
            addFileExpire(fileInfoRedisEntities);
            return fileInfoRedisEntities;
        }

        return null;
    }

    private void addFileLabel(Long busiId, List<FileInfoRedisDTO> fileInfoRedisEntities) {
        List<EcmFileLabel> ecmFileLabels = ecmFileLabelMapper.selectList(new LambdaQueryWrapper<EcmFileLabel>().eq(EcmFileLabel::getBusiId, busiId));
        if (CollectionUtils.isEmpty(ecmFileLabels)) {
            return;
        }
        Map<Long, List<EcmFileLabel>> collect = ecmFileLabels.stream().collect(Collectors.groupingBy(EcmFileLabel::getFileId));
        for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedisEntities) {
            fileInfoRedisDTO.setEcmFileLabels(collect.get(fileInfoRedisDTO.getFileId()));
        }
    }

    private void addFileCommentCount(List<FileInfoRedisDTO> fileInfoRedisEntities) {
        List<Long> newFileIds = fileInfoRedisEntities.stream().map(EcmFileInfoDTO::getNewFileId).collect(Collectors.toList());
        //获取所有批注信息
        List<EcmFileComment> ecmFileComments = ecmFileCommentMapper.selectList(new LambdaQueryWrapper<EcmFileComment>()
                .eq(EcmFileComment::getBusiId, fileInfoRedisEntities.get(0).getBusiId()));
        //分组
        Map<Long, List<EcmFileComment>> groupedByComment = ecmFileComments.stream().filter(s -> newFileIds.contains(s.getNewFileId())).collect(Collectors.groupingBy(EcmFileComment::getNewFileId));
        if (!CollectionUtils.isEmpty(groupedByComment.keySet())) {
            for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedisEntities) {
                List<EcmFileComment> fileComments = groupedByComment.get(fileInfoRedisDTO.getNewFileId());
                if (!CollectionUtils.isEmpty(fileComments)) {
                    //添加文件批注数量
                    fileInfoRedisDTO.setFileCommentCount(fileComments.size());
                }
            }
        }

    }

    private void addFileExpire(List<FileInfoRedisDTO> fileInfoRedisEntities) {
        List<Long> fileIds = fileInfoRedisEntities.stream().map(EcmFileInfoDTO::getFileId).collect(Collectors.toList());
        List<EcmFileExpireInfo> ecmFileExpireInfos = ecmFileExpireInfoMapper.selectList(new LambdaQueryWrapper<EcmFileExpireInfo>()
                .in(EcmFileExpireInfo::getFileId, fileIds));
        if(CollectionUtils.isEmpty(ecmFileExpireInfos)){
            fileInfoRedisEntities.forEach(s->s.setIsExpired(StateConstants.ZERO));
            return;
        }
        Map<Long, EcmFileExpireInfo> fileExpireMap = ecmFileExpireInfos.stream()
                .collect(Collectors.toMap(EcmFileExpireInfo::getFileId, Function.identity()));
        List<Long> collect = ecmFileExpireInfos.stream().map(EcmFileExpireInfo::getFileId).collect(Collectors.toList());
        for(FileInfoRedisDTO dto : fileInfoRedisEntities){
            if(collect.contains(dto.getFileId())){
                dto.setExpireDate(fileExpireMap.get(dto.getFileId()).getExpireDate());
                dto.setIsExpired(fileExpireMap.get(dto.getFileId()).getIsExpired());
            }else {
                dto.setIsExpired(StateConstants.ZERO);
            }
        }
    }

    private void addFileHistory(List<FileInfoRedisDTO> fileInfoRedisEntities, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO, Map<Long, String> fileIdDocFileSortMap, Long busiId) {
        AssertUtils.isNull(ecmBusiInfoRedisDTO.getAppCode(), "appCode不能为空");
        AssertUtils.isNull(ecmBusiInfoRedisDTO.getBusiNo(), "busiNo不能为空");
        List<Long> fileIds = fileInfoRedisEntities.stream().map(EcmFileInfoDTO::getFileId).collect(Collectors.toList());
        List<EcmFileHistory> fileHistories = new ArrayList<>(); // 存储所有查询结果的集合
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        // 每批次查询的数据量
//        int batchSize = 10000;
//        // 总数据量
//        int totalSize = fileIds.size();
        //如果文件历史数量大于10000，怎分批次查询历史数据
//        if (totalSize <= batchSize) {
//            List<EcmFileHistory> batchResult = ecmFileHistoryMapper.selectList(new LambdaQueryWrapper<EcmFileHistory>()
//                    .eq(EcmFileHistory::getBusiId, busiId)
//                    .in(EcmFileHistory::getFileId, fileIds));
//
//            fileHistories.addAll(batchResult);
//        } else {
//            for (int i = 0; i < totalSize; i += batchSize) {
//                int endIndex = Math.min(i + batchSize, totalSize);
//                List<Long> sublist = fileIds.subList(i, endIndex);
//                List<EcmFileHistory> batchResult = ecmFileHistoryMapper.selectList(new LambdaQueryWrapper<EcmFileHistory>()
//                        .eq(EcmFileHistory::getBusiId, busiId)
//                        .in(EcmFileHistory::getFileId, sublist));
//                fileHistories.addAll(batchResult);
//            }
//        }

        List<EcmFileHistory> batchResult = ecmFileHistoryMapper.selectList(new LambdaQueryWrapper<EcmFileHistory>()
                .eq(EcmFileHistory::getBusiId, busiId));
        fileHistories = batchResult.stream().filter(s -> fileIds.contains(s.getFileId())).collect(Collectors.toList());

        Map<Long, List<EcmFileHistory>> groupedByFileId = fileHistories.stream().collect(Collectors.groupingBy(EcmFileHistory::getFileId));

        //业务类型信息
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(ecmBusiInfoRedisDTO.getAppCode());

        Map<String, EcmDocDef> docCodeListMap = null;
        Map<String, List<EcmBusiDoc>> collect = null;
        if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
            //查询资料类型信息
            //分组
            docCodeListMap = getDocInfoAll();
            AssertUtils.isNull(docCodeListMap, "EcmDocDef表数据为空");
        } else {
            //查询资料类型信息
            List<EcmBusiDoc> ecmDocDefs = ecmBusiDocMapper.selectList(new QueryWrapper<EcmBusiDoc>()
                    .eq("busi_id", ecmBusiInfoRedisDTO.getBusiId()));
            collect = ecmDocDefs.stream().collect(Collectors.groupingBy(EcmBusiDoc::getDocCode));

        }
        //设置文件顺序
        for (FileInfoRedisDTO fileInfoRedisDTO : fileInfoRedisEntities) {
            String docCode = fileInfoRedisDTO.getDocCode();
            List<EcmFileHistory> fileHistories1 = groupedByFileId.get(fileInfoRedisDTO.getFileId());
            if (!CollectionUtils.isEmpty(fileHistories1)) {
                //排序
                fileHistories1 = fileHistories1.stream().sorted(Comparator.comparing(EcmFileHistory::getCreateTime)).collect(Collectors.toList());
                fileInfoRedisDTO.setFileHistories(fileHistories1);
            }
            fileInfoRedisDTO.setAppCode(ecmAppDef.getAppCode());
            fileInfoRedisDTO.setAppTypeName(ecmAppDef.getAppName());
            fileInfoRedisDTO.setBusiNo(ecmBusiInfoRedisDTO.getBusiNo());
            fileInfoRedisDTO.setOrgCode(ecmBusiInfoRedisDTO.getOrgCode());
            //未归类
            if (IcmsConstants.UNCLASSIFIED_ID.equals(docCode)) {
                fileInfoRedisDTO.setDocName(IcmsConstants.UNCLASSIFIED);
            } else {
                if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
                    EcmDocDef ecmDocDefs1 = docCodeListMap.get(docCode);
                    AssertUtils.isNull(ecmDocDefs1, "参数错误，资料节点为空");
                    fileInfoRedisDTO.setDocName(ecmDocDefs1.getDocName());
                    fileInfoRedisDTO.setDocId(docCode);
                } else {
                    //动态树
                    List<EcmBusiDoc> ecmBusiDocs = collect.get(docCode);
                    AssertUtils.isNull(ecmBusiDocs, "参数错误，资料节点为空");
                    fileInfoRedisDTO.setDocName(ecmBusiDocs.get(0).getDocName());
                    fileInfoRedisDTO.setDocId(ecmBusiDocs.get(0).getDocId().toString());
                }
            }

            fileInfoRedisDTO.setSignFlag(IcmsConstants.SIGN_FLAG_ONE);
            //添加docFileSort
            if (!ObjectUtils.isEmpty(fileIdDocFileSortMap)) {
                fileInfoRedisDTO.setDocFileSort(fileIdDocFileSortMap.get(fileInfoRedisDTO.getFileId()));
            }
        }

    }


    /**
     * 获取业务对应的版本
     */
    public List<EcmDocrightDefDTO> getEcmDocrightDefDTOS(AccountTokenExtendDTO token, EcmBusiInfoRedisDTO busiInfoRedisDTO) {
        List<EcmDocrightDefDTO> ecmDocrightDefDTOList = new ArrayList<>();
        //获取该版本对应的资料权限
        Integer rightVer = busiInfoRedisDTO.getRightVer();
        ecmDocrightDefDTOList = staticTreePermissService.roleDimLogic2(busiInfoRedisDTO.getAppCode(), rightVer, token);
        return ecmDocrightDefDTOList;
    }

    /**
     * 获取资料权限
     */
    public List<EcmDocrightDefDTO> getDocrightDefCommon(EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO, AccountTokenExtendDTO token) {
        List<EcmDocrightDefDTO> docRightList = null;
        if (IcmsConstants.STATIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
            //静态树
                UserBusiRedisDTO userPageRedis = getUserPageRedis(token.getFlagId(), token);
                docRightList = userPageRedis.getDocRightList().get(ecmBusiInfoRedisDTO.getBusiId());
                if(CollectionUtils.isEmpty(docRightList)){
                    //其他业务的权限，非登陆业务的
                    docRightList = staticTreePermissService.roleDimLogic2(ecmBusiInfoRedisDTO.getAppCode(), ecmBusiInfoRedisDTO.getRightVer(), token);
                }
        } else if (IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
            //动态树
            if (token.isOut()) {
                UserBusiRedisDTO userPageRedis = getUserPageRedis(token.getFlagId(), token);
                docRightList = userPageRedis.getDocRightList().get(ecmBusiInfoRedisDTO.getBusiId());
            } else {
                UserBusiRedisDTO userPageRedis = getUserPageRedis(token.getFlagId(), token);
                docRightList = userPageRedis.getDocRightList().get(ecmBusiInfoRedisDTO.getBusiId());
                //pc端打开，全权限开放
//                docRightList = ecmBusiInfoRedisDTO.getDocRightList();
                if (!org.springframework.util.CollectionUtils.isEmpty(docRightList)) {
                    docRightList.forEach(s -> {
                        if (s.getAddRight() == null) {
                            s.setAddRight(StateConstants.YES.toString());
                        }
                        if (s.getUpdateRight() == null) {
                            s.setUpdateRight(StateConstants.YES.toString());
                        }
                        if (s.getReadRight() == null) {
                            s.setReadRight(StateConstants.YES.toString());
                        }
                        if (s.getDeleteRight() == null) {
                            s.setDeleteRight(StateConstants.YES.toString());
                        }
                        if (s.getDownloadRight() == null) {
                            s.setDownloadRight(StateConstants.YES.toString());
                        }
                        if (s.getPrintRight() == null) {
                            s.setPrintRight(StateConstants.YES.toString());
                        }
                        if (s.getThumRight() == null) {
                            s.setThumRight(StateConstants.YES.toString());
                        }
                        if (s.getOtherUpdate() == null) {
                            s.setOtherUpdate(StateConstants.YES.toString());
                        }
                    });
                } else {
//                    UserBusiRedisDTO userPageRedis = getUserPageRedis(token.getFlagId());
//                    docRightList = userPageRedis.getDocRightList().get(ecmBusiInfoRedisDTO.getBusiId());
//                    ecmBusiInfoRedisDTO.setDocRightList(docRightList);
//                    saveAndUpate(ecmBusiInfoRedisDTO);
                }
            }
        }
//        AssertUtils.isNull(docRightList, "资料节点数据为空");
        return docRightList;
    }

    /**
     * 获取用户业务信息
     */
    public UserBusiRedisDTO getUserPageRedis(String key, AccountTokenExtendDTO token) {
        UserBusiRedisDTO userBusiRedisDTO = null;
        if (redisUtils.hasKey(RedisConstants.USER_BUSI_PREFIX + key)) {
            Object hget3 = redisUtils.hget(RedisConstants.USER_BUSI_PREFIX + key, RedisConstants.USER_BUSI_PREFIX + key);
            if (hget3 != null) {
                userBusiRedisDTO = (UserBusiRedisDTO) hget3;
                if (CollectionUtils.isEmpty(userBusiRedisDTO.getRoleIds())) {
                    SysUserDTO dto = new SysUserDTO();
                    dto.setLoginName(userBusiRedisDTO.getUsercode());
                    dto.setRoleCodeList(userBusiRedisDTO.getRole());
                    dto.setInstNo(userBusiRedisDTO.getOrg());
                    Result<SysUserDTO> sysUserDTOResult;
                    if (null == token || token.isOut()) {
                        sysUserDTOResult = userApi.checkUserSpecial(dto);
                    } else {
                        sysUserDTOResult = userApi.checkUser(dto);
                    }
                    if (sysUserDTOResult.isSucc() && sysUserDTOResult.getData() != null) {
                        userBusiRedisDTO.setRoleIds(sysUserDTOResult.getData().getRoleIdList());
                        saveOrUpdateUser(key, userBusiRedisDTO);
                    }
                }
                return userBusiRedisDTO;
            }

        }
        AssertUtils.isNull(userBusiRedisDTO, "当前登陆已失效，请重新打开采集页面");
        return userBusiRedisDTO;
    }

    /**
     * 保存用户信息
     */
    public UserBusiRedisDTO saveOrUpdateUser(String key, UserBusiRedisDTO userBusiRedisDTO) {
        redisUtils.hset(RedisConstants.USER_BUSI_PREFIX + key, RedisConstants.USER_BUSI_PREFIX + key, userBusiRedisDTO, TimeOutConstants.ONE_DAY);
        return userBusiRedisDTO;

    }

    /**
     * 校验用户
     */
    public AccountTokenExtendDTO checkUser(EcmUserDTO ecmBaseInfoDTO, AccountTokenExtendDTO token) {
        if (ecmBaseInfoDTO == null) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "用户信息不能为空!");
        }
        if (StrUtil.isBlank(ecmBaseInfoDTO.getOrgCode())) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "机构号不能为空!");
        }
        if (StrUtil.isBlank(ecmBaseInfoDTO.getOrgName())) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "机构名称不能为空!");
        }
        if (StrUtil.isBlank(ecmBaseInfoDTO.getUserCode())) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "用户码不能为空!");
        }
        if (StrUtil.isBlank(ecmBaseInfoDTO.getUserName())) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "用户名称不能为空!");
        }
        if (org.springframework.util.CollectionUtils.isEmpty(ecmBaseInfoDTO.getRoleCode())) {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, "角色编码不能为空!");
        }
        SysUserDTO sysUserDTO = new SysUserDTO();
        sysUserDTO.setInstNo(ecmBaseInfoDTO.getOrgCode());
        sysUserDTO.setRoleCodeList(ecmBaseInfoDTO.getRoleCode());
        sysUserDTO.setLoginName(ecmBaseInfoDTO.getUserCode());
        Result<SysUserDTO> sysUserDTOResult;

        if (null == token || token.isOut()) {
            sysUserDTOResult = userApi.checkUserSpecial(sysUserDTO);
        } else {
            sysUserDTOResult = userApi.checkUser(sysUserDTO);
        }
        if (sysUserDTOResult.isSucc() && sysUserDTOResult.getData() != null) {
            SysUserDTO data = sysUserDTOResult.getData();
            if (token == null) {
                token = new AccountTokenExtendDTO();
            }
            token.setName(data.getName());
            token.setOrgName(data.getInstName());
            token.setInstId(data.getInstId());
            token.setRoleIdList(data.getRoleIdList());
            token.setRoleCodeList(data.getRoleCodeList());
            if (token.getFlagId() != null) {
                String flagId = token.getFlagId();
                UserBusiRedisDTO userPageRedis = getUserPageRedis(flagId, token);
                userPageRedis.setRoleIds(data.getRoleIdList());
                userPageRedis.setRole(data.getRoleCodeList());
                saveOrUpdateUser(token.getFlagId(), userPageRedis);
            }
            return token;
        } else {
            throw new SunyardException(ResultCode.NO_DATA_AUTH, sysUserDTOResult.getMsg());
        }
    }

    /**
     * 获取业务数据
     */
    public EcmBusiInfoRedisDTO getEcmBusiInfoRedisDTO(AccountTokenExtendDTO token, Long busiId2) {
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = null;
        if (redisUtils.hasKey(RedisConstants.BUSI_BASEINFO_PREFIX + busiId2)) {
            Object hget3 = redisUtils.hget(RedisConstants.BUSI_BASEINFO_PREFIX + busiId2, RedisConstants.BUSI_BASEINFO_PREFIX + busiId2);
            if (hget3 != null) {
                ecmBusiInfoRedisDTO = (EcmBusiInfoRedisDTO) hget3;
//                if (ecmBusiInfoRedisDTO.getDocRightList() == null && token.getFlagId() != null) {
//                    UserBusiRedisDTO userPageRedis = getUserPageRedis(token.getFlagId(), token);
//                    List<EcmDocrightDefDTO> ecmDocrightDefDTOS = userPageRedis.getDocRightList().get(busiId2);
//                    ecmBusiInfoRedisDTO.setDocRightList(ecmDocrightDefDTOS);
//                    saveAndUpate(ecmBusiInfoRedisDTO);
//
//                }
            }

        }
        if (ecmBusiInfoRedisDTO == null) {
            ecmBusiInfoRedisDTO = followPerDb(busiId2, token, null);
        }
        AssertUtils.isNull(ecmBusiInfoRedisDTO, "当前业务不存在");
        return ecmBusiInfoRedisDTO;
    }

    /**
     * 更新redis
     */
    public EcmBusiInfoRedisDTO saveAndUpate(EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        redisUtils.hset(RedisConstants.BUSI_BASEINFO_PREFIX + ecmBusiInfoRedisDTO.getBusiId(), RedisConstants.BUSI_BASEINFO_PREFIX + ecmBusiInfoRedisDTO.getBusiId(), ecmBusiInfoRedisDTO, TimeOutConstants.ONE_DAY);
        return ecmBusiInfoRedisDTO;
    }
    public void updateRedisBusiStatus(EcmBusiInfo ecmBusiInfo, AccountTokenExtendDTO token, Integer status) {
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = getEcmBusiInfoRedisDTO(token,
                ecmBusiInfo.getBusiId());
        ecmBusiInfoRedisDTO.setStatus(status);
        //更新就近修改人
        ecmBusiInfoRedisDTO.setUpdateUser(token.getUsername());
        //更新最近修改时间
        ecmBusiInfoRedisDTO.setUpdateTime(new Date());
        saveAndUpate(ecmBusiInfoRedisDTO);
    }
    /**
     * 根据appcode获取业务属性
     */
    public List<EcmAppAttrDTO> getAppAttrExtends(String appCode, Long busiId) {
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>()
                .eq(EcmAppAttr::getAppCode, appCode));
        if (CollectionUtils.isEmpty(ecmAppAttrs)) {
            return null;
        }
        List<EcmBusiMetadata> busiMetadata = ecmBusiMetadataMapper.selectList(new LambdaUpdateWrapper<EcmBusiMetadata>()
                .eq(EcmBusiMetadata::getBusiId, busiId));
        if (CollectionUtils.isEmpty(busiMetadata)) {
            return null;
        }
        List<EcmAppAttrDTO> ecmAppAttrDTOS = PageCopyListUtils.copyListProperties(ecmAppAttrs, EcmAppAttrDTO.class);
        Map<Long, List<EcmBusiMetadata>> groupedByAppAttrId = busiMetadata.stream().collect(Collectors.groupingBy(EcmBusiMetadata::getAppAttrId));
        for (EcmAppAttrDTO extend : ecmAppAttrDTOS) {
            List<EcmBusiMetadata> ecmBusiMetadata = groupedByAppAttrId.get(extend.getAppAttrId());
            if (!CollectionUtils.isEmpty(ecmBusiMetadata)) {
                extend.setAppAttrValue(ecmBusiMetadata.get(0).getAppAttrVal());
            }
        }
        return ecmAppAttrDTOS;
    }

    /**
     * 根据appcode和批量busiId获取业务属性，返回按busiId分组的结果
     * @param busiIds 业务ID列表
     * @return Map<Long, List<EcmAppAttrDTO>> key为busiId，value为对应的业务属性列表
     */
    public Map<Long, List<EcmAppAttrDTO>> getAppAttrExtends(List<Long> busiIds) {
        // 初始化返回结果，避免返回null
        Map<Long, List<EcmAppAttrDTO>> resultMap = new HashMap<>();

        // 入参校验：busiIds为空直接返回空map
        if (CollectionUtils.isEmpty(busiIds)) {
            return resultMap;
        }

        // 1. 查询appCodes下的所有基础属性
        List<EcmAppAttr> ecmAppAttrs = ecmAppAttrMapper.selectList(new LambdaQueryWrapper<EcmAppAttr>());
        if (CollectionUtils.isEmpty(ecmAppAttrs)) {
            // 没有基础属性时，为每个busiId初始化空列表
            busiIds.forEach(busiId -> resultMap.put(busiId, new ArrayList<>()));
            return resultMap;
        }

        // 2. 批量查询所有busiId对应的元数据（一次查询，避免循环查库）
        List<EcmBusiMetadata> busiMetadataList = ecmBusiMetadataMapper.selectList(new LambdaQueryWrapper<EcmBusiMetadata>()
                .in(EcmBusiMetadata::getBusiId, busiIds));

        // 3. 按busiId + appAttrId分组元数据，方便后续匹配（双层分组）
        Map<Long, Map<Long, EcmBusiMetadata>> busiIdToAttrIdMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(busiMetadataList)) {
            busiIdToAttrIdMap = busiMetadataList.stream()
                    .collect(Collectors.groupingBy(
                            EcmBusiMetadata::getBusiId,  // 第一层：按busiId分组
                            Collectors.toMap(
                                    EcmBusiMetadata::getAppAttrId,  // 第二层：按appAttrId分组
                                    metadata -> metadata,  // 值为元数据本身
                                    (existing, replacement) -> existing  // 重复时保留第一个
                            )
                    ));
        }

        // 4. 为每个busiId构建对应的属性列表
        for (Long busiId : busiIds) {
            // 复制基础属性到DTO列表
            List<EcmAppAttrDTO> attrDTOList = PageCopyListUtils.copyListProperties(ecmAppAttrs, EcmAppAttrDTO.class);

            // 获取当前busiId对应的元数据映射
            Map<Long, EcmBusiMetadata> attrIdToMetadataMap = busiIdToAttrIdMap.getOrDefault(busiId, new HashMap<>());

            // 填充每个属性的value值
            for (EcmAppAttrDTO attrDTO : attrDTOList) {
                EcmBusiMetadata metadata = attrIdToMetadataMap.get(attrDTO.getAppAttrId());
                if (metadata != null) {
                    attrDTO.setAppAttrValue(metadata.getAppAttrVal());
                }
            }

            // 将构建好的列表放入结果map
            resultMap.put(busiId, attrDTOList);
        }

        return resultMap;
    }

    private void addBusiAttrToRedis(EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        if (!CollectionUtils.isEmpty(ecmBusiInfoExtend.getAttrList())) {
            return;
        }
        List<EcmAppAttrDTO> ecmAppAttrDTOS = getAppAttrExtends(ecmBusiInfoExtend.getAppCode(), ecmBusiInfoExtend.getBusiId());
        ecmBusiInfoExtend.setAttrList(ecmAppAttrDTOS);
    }

    private void addBusiTrajectoryToRedis(EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        List<EcmBusiVersion> busiVersions = ecmBusiVersionMapper.selectList(new LambdaQueryWrapper<EcmBusiVersion>()
                .eq(EcmBusiVersion::getBusiId, ecmBusiInfoExtend.getBusiId()));
        if (!CollectionUtils.isEmpty(busiVersions)) {
            ecmBusiInfoExtend.setEcmBusiVersions(busiVersions);
        }
    }

    /**
     * 获取存储服务中文件的基本信息并添加到影像文件信息对象中
     */
    public void getStorageFileInfo(List<EcmFileInfoDTO> fileInfoExtends) {
        //文件id列表
        List<Long> fileIds = new ArrayList<>();
        //资料节点列表
        List<String> docIds = new ArrayList<>();
        fileInfoExtends.forEach(p -> {
            if (!ObjectUtils.isEmpty(p.getNewFileId())) {
                fileIds.add(p.getNewFileId());
            }
            if (!ObjectUtils.isEmpty(p.getDocId())) {
                docIds.add(p.getDocId());
            }
        });

        //获取业务信息表数据
        EcmBusiInfo busiInfo = ecmBusiInfoMapper.selectById(fileInfoExtends.get(0).getBusiId());
        for (EcmFileInfoDTO extend : fileInfoExtends) {
            //bytes转换成KB
            extend.setSizeStr((extend.getNewFileSize() / 1024) + "KB");
            extend.setSize(extend.getNewFileSize());

            String suffix = FilenameUtils.getExtension(extend.getNewFileName());
            //文件格式
            extend.setFormat(suffix);
            extend.setSourceFileMd5(extend.getFileMd5());
            extend.setTreeType(busiInfo.getTreeType().toString());
            if (!ObjectUtils.isEmpty(busiInfo)) {
                //添加资料权限版本号
                extend.setRightVer(busiInfo.getRightVer());
            }
            if (IcmsConstants.DYNAMIC_TREE.equals(busiInfo.getTreeType())) {
                Map<Long, List<EcmBusiDoc>> groupedByDocId = new HashMap<>();
                if (!CollectionUtils.isEmpty(docIds)) {
                    List<EcmBusiDoc> busiDocs = ecmBusiDocMapper.selectBatchIds(docIds);
                    if (!CollectionUtils.isEmpty(busiDocs)) {
                        groupedByDocId = busiDocs.stream().collect(Collectors.groupingBy(EcmBusiDoc::getDocId));
                    }
                }
                //资料类型名称
                if (extend.getDocId() != null) {
                    List<EcmBusiDoc> busiDocs1 = groupedByDocId.get(Long.parseLong(extend.getDocId()));
                    if (!CollectionUtils.isEmpty(busiDocs1)) {
                        extend.setDocName(busiDocs1.get(0).getDocName());
                        extend.setDocCode(busiDocs1.get(0).getDocCode());
                    }
                }
            } else {
            }

        }
    }

    /**
     * 获取压缩比
     */
    public EcmAppDef getRedisZip(String appCode) {
        AssertUtils.isNull(appCode, "业务类型参数不能为空");
        //全剧压缩则用全局压缩TEM
        //判断是否需要压缩
        SysStrategyDTO sysStrategyDTO = sysStrategyService.queryConfig();
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(appCode);
        if (sysStrategyDTO.getZipStatus()) {
            ecmAppDef.setIsResize(sysStrategyDTO.getZipStatus() ? StateConstants.YES : StateConstants.NO);
            ecmAppDef.setResize(sysStrategyDTO.getZipBound());
            if (sysStrategyDTO.getZipScale() != null) {
                double v = sysStrategyDTO.getZipScale() * 0.01;
                ecmAppDef.setQulity(Float.parseFloat(v + ""));
            } else {
                ecmAppDef.setQulity(1f);
            }
        }
        return ecmAppDef;
    }

    /**
     * 获取redis中的文件，业务下指定的文件列表
     */
    public List<FileInfoRedisDTO> getFileInfoRedis(Long buisId, List<Long> fileId) {
        existsKeyFileInfoRedis(buisId);
        List<FileInfoRedisDTO> fileInfoRedisDTOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(fileId)) {
            List<Long> fileIdNo = new ArrayList<>();
            for (Long id : fileId) {
                Object hget = redisUtils.hget(RedisConstants.BUSIFILE_PREFIX + buisId, id.toString());
                if (hget != null) {
                    FileInfoRedisDTO dto = (FileInfoRedisDTO) hget;
                    fileInfoRedisDTOS.add(dto);
                } else {
                    //如果文件不存在，去数据库查询
                    //获取所有文件
                    //重新获取
                    fileIdNo.add(id);
                }
            }
            if (!CollectionUtils.isEmpty(fileIdNo)) {
                //存在不存在的redis中的文件
                EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(buisId);
                if (ObjectUtils.isEmpty(ecmBusiInfo)) {
                    return null;
                }
                EcmBusiInfoRedisDTO ecmBusiInfoExtend = new EcmBusiInfoRedisDTO();
                BeanUtils.copyProperties(ecmBusiInfo, ecmBusiInfoExtend);
                List<EcmFileInfo> fileInfoList = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>()
                        .eq(EcmFileInfo::getBusiId, ecmBusiInfoExtend.getBusiId()).in(EcmFileInfo::getFileId, fileIdNo));
                if (!CollectionUtils.isEmpty(fileInfoList)) {
                    List<EcmFileInfoDTO> fileInfoExtends = PageCopyListUtils.copyListProperties(fileInfoList, EcmFileInfoDTO.class);
                    //获取存储服务中文件的基本信息并添加到影像文件信息对象中
                    getStorageFileInfo(fileInfoExtends);
                    List<FileInfoRedisDTO> fileInfoRedisEntities = PageCopyListUtils.copyListProperties(fileInfoExtends, FileInfoRedisDTO.class);
                    //添加文件历史、 添加业务数据：appCode、appTypeName、busiNo
                    addFileHistory(fileInfoRedisEntities, ecmBusiInfoExtend, null, ecmBusiInfoExtend.getBusiId());
                    //添加文件批注数量
                    addFileCommentCount(fileInfoRedisEntities);
                    //添加文件期限
                    addFileExpire(fileInfoRedisEntities);
                    fileInfoRedisDTOS.addAll(fileInfoRedisEntities);
                }
            }


        } else {
            Map<Object, Object> hmget = redisUtils.hmget(RedisConstants.BUSIFILE_PREFIX + buisId);
            if (hmget != null) {
                for (Object object : hmget.keySet()) {
                    String object2 = (String) object;
                    FileInfoRedisDTO dto = (FileInfoRedisDTO) hmget.get(object2);
                    fileInfoRedisDTOS.add(dto);
                }
            }
            Long l = ecmFileInfoMapper.selectCount(new LambdaQueryWrapper<EcmFileInfo>().eq(EcmFileInfo::getBusiId, buisId));
            if (l != fileInfoRedisDTOS.size() && l != 0) {
                //重新获取
                EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(buisId);
                if (ObjectUtils.isEmpty(ecmBusiInfo)) {
                    return null;
                }
                EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = new EcmBusiInfoRedisDTO();
                BeanUtils.copyProperties(ecmBusiInfo, ecmBusiInfoRedisDTO);
                List<FileInfoRedisDTO> fileInfoRedisEntities = savefile(buisId, ecmBusiInfoRedisDTO);
                fileInfoRedisDTOS = fileInfoRedisEntities;
            }

        }

        return fileInfoRedisDTOS;
    }

    private List<FileInfoRedisDTO> savefile(Long buisId, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        List<FileInfoRedisDTO> fileInfoRedisEntities = getFileInfoRedis(ecmBusiInfoRedisDTO, null);
        if (!CollectionUtils.isEmpty(fileInfoRedisEntities)) {
            Map<String, Object> map = new HashMap<>();
            for (FileInfoRedisDTO dto : fileInfoRedisEntities) {
                map.put(dto.getFileId().toString(), dto);
            }
            redisUtils.hmset(RedisConstants.BUSIFILE_PREFIX + buisId, map, TimeOutConstants.ONE_DAY);
        }
        return fileInfoRedisEntities;
    }

    /**
     * 获取redis中的文件，业务下指定的某一个文件
     */
    public FileInfoRedisDTO getFileInfoRedisSingle(Long buisId, Long fileId) {
        ArrayList<Long> objects = new ArrayList<>();
        objects.add(fileId);
        List<FileInfoRedisDTO> fileInfoRedis = getFileInfoRedis(buisId, objects);
        if (!CollectionUtils.isEmpty(fileInfoRedis)) {
            return fileInfoRedis.get(0);
        }
        return null;
    }

    /**
     * 获取redis中的文件，业务下所有文件
     */
    public List<FileInfoRedisDTO> getFileInfoRedis(Long buisId) {
        return getFileInfoRedis(buisId, null);
    }

    public List<FileInfoRedisDTO> getFileInfoRedisByFileIds(Long buisId,List<Long> fileid) {
        return getFileInfoRedis(buisId, fileid);
    }

    /**
     * 更新redis中的文件
     */
    public void updateFileInfoRedis(FileInfoRedisDTO fileInfoRedisDTO) {
        //        如果不存在都不需要更新,在获取的时候会从数据库中重新加载
        Boolean b = redisUtils.hasKey(RedisConstants.BUSIFILE_PREFIX + fileInfoRedisDTO.getBusiId());
        if (b) {
            Boolean c = redisUtils.hHasKey(RedisConstants.BUSIFILE_PREFIX + fileInfoRedisDTO.getBusiId(), fileInfoRedisDTO.getFileId().toString());
            if (c) {
                redisUtils.hset(RedisConstants.BUSIFILE_PREFIX + fileInfoRedisDTO.getBusiId(),
                        fileInfoRedisDTO.getFileId().toString(),
                        fileInfoRedisDTO, TimeOutConstants.ONE_DAY);
            }
        }

    }

    public void saveFileInfoRedis(FileInfoRedisDTO fileInfoRedisDTO) {
        //        如果不存在都不需要更新,在获取的时候会从数据库中重新加载
        Boolean b = redisUtils.hasKey(RedisConstants.BUSIFILE_PREFIX + fileInfoRedisDTO.getBusiId());
        if (b) {
            Boolean c = redisUtils.hHasKey(RedisConstants.BUSIFILE_PREFIX + fileInfoRedisDTO.getBusiId(), fileInfoRedisDTO.getFileId().toString());
            if (!c) {
                redisUtils.hset(RedisConstants.BUSIFILE_PREFIX + fileInfoRedisDTO.getBusiId(),
                        fileInfoRedisDTO.getFileId().toString(),
                        fileInfoRedisDTO, TimeOutConstants.ONE_DAY);
            }
        }

    }

    /**
     * 更新redis中的文件，按列表更新
     */
    public void updateFileInfoRedis(List<FileInfoRedisDTO> fileInfoRedisDTO) {
        for (FileInfoRedisDTO fileInfoRedisDTO1 : fileInfoRedisDTO) {
            updateFileInfoRedis(fileInfoRedisDTO1);
        }
    }

    private void existsKeyFileInfoRedis(Long buisId) {
        Boolean b = redisUtils.hasKey(RedisConstants.BUSIFILE_PREFIX + buisId);
        if (!b) {
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectById(buisId);
            if (ObjectUtils.isEmpty(ecmBusiInfo)) {
                return;
            }
            EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO = new EcmBusiInfoRedisDTO();
            BeanUtils.copyProperties(ecmBusiInfo, ecmBusiInfoRedisDTO);
            savefile(buisId, ecmBusiInfoRedisDTO);
        }

    }

    private List<EcmAsyncTask> getAsyncTaskRedis(Long buisId) {
        Boolean b = redisUtils.hasKey(RedisConstants.BUSIASYNC_TASK_PREFIX + buisId);
        List<EcmAsyncTask> ecmAsyncTasks = new ArrayList<>();

        if (!b) {
            // 如果 Redis 中不存在，从数据库查询并存入 Redis
            ecmAsyncTasks = asyncTaskMapper.selectList(new LambdaQueryWrapper<EcmAsyncTask>().eq(EcmAsyncTask::getBusiId, buisId));
            if (CollectionUtils.isEmpty(ecmAsyncTasks)) {
                return ecmAsyncTasks;
            }
            Map<String, Object> map = new HashMap<>();
            for (EcmAsyncTask dto : ecmAsyncTasks) {
                map.put(dto.getFileId().toString(), dto);
            }
            redisUtils.hmset(RedisConstants.BUSIASYNC_TASK_PREFIX + buisId, map, TimeOutConstants.THERR_MONTH);
        } else {
            // 如果 Redis 中存在，直接从 Redis 获取数据
            Map<Object, Object> redisMap = redisUtils.hmget(RedisConstants.BUSIASYNC_TASK_PREFIX + buisId);
            if (redisMap != null && !redisMap.isEmpty()) {
                for (Map.Entry<Object, Object> entry : redisMap.entrySet()) {
                    if (entry.getValue() instanceof EcmAsyncTask) {
                        ecmAsyncTasks.add((EcmAsyncTask) entry.getValue());
                    }
                }
            }
        }
        return ecmAsyncTasks;
    }

    /**
     * 删除redis指定业务下的文件列表
     */
    public List<FileInfoRedisDTO> delFileInfoRedis(Long buisId, List<Long> delFileIdList) {
        existsKeyFileInfoRedis(buisId);
        List<FileInfoRedisDTO> dtos = new ArrayList<>();
        for (Long id : delFileIdList) {
            synchronized (id) {
                Object hget = redisUtils.hget(RedisConstants.BUSIFILE_PREFIX + buisId, id.toString());
                if (hget != null) {
                    FileInfoRedisDTO dto = (FileInfoRedisDTO) hget;
                    dto.setState(IcmsConstants.ONE);
                    dtos.add(dto);
                    redisUtils.hset(RedisConstants.BUSIFILE_PREFIX + buisId, id.toString(), dto, TimeOutConstants.ONE_DAY);
                }
            }
        }
        return dtos;

    }

    /**
     * 移动文件，将老业务中对应的文件移除
     */
    public List<FileInfoRedisDTO> delFileInfoRedisReal(Long buisId, List<Long> delFileIdList) {
        existsKeyFileInfoRedis(buisId);
        List<FileInfoRedisDTO> dtos = new ArrayList<>();
        for (Long id : delFileIdList) {
            synchronized (id) {
                Object hget = redisUtils.hget(RedisConstants.BUSIFILE_PREFIX + buisId, id.toString());
                if (hget != null) {
                    redisUtils.hdel(RedisConstants.BUSIFILE_PREFIX + buisId, id.toString());
                }
            }
        }
        return dtos;

    }

    /**
     * 删除redis指定key
     */
    public void delFileInfoRedisReal(Long buisId) {
        Boolean b = redisUtils.hasKey(RedisConstants.BUSIFILE_PREFIX + buisId);
        if (b) {
            redisUtils.del(RedisConstants.BUSIFILE_PREFIX + buisId);
        }
    }

    /**
     * 删除指定业务
     */
    public void delBusiInfo(Long busiId) {
        if (redisUtils.hasKey(RedisConstants.BUSI_BASEINFO_PREFIX + busiId)) {
            redisUtils.del(RedisConstants.BUSI_BASEINFO_PREFIX + busiId);
        }
    }

    /**
     * 根据busiId获取缓存中的异步任务列表
     */
    public List<EcmAsyncTask> getEcmAsyncTaskList(Long busiId) {
        return getAsyncTaskRedis(busiId);
    }

    /**
     * 获取需要推送异步任务的业务列表keys
     */
    public List<String> getNeedPushEcmAsyncTaskList() {
        Set<String> kes = redisUtils.executeWithStickyConnection(RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX);
        List<String> busiIds = new ArrayList<>();
        for (String key : kes) {
            String busiId = key.substring(RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX.length());
            busiIds.add(busiId);
        }
        return busiIds;
    }

    /**
     * 判断是否有自动归类开关
     */
    public Boolean hasAutoGroup(String key) {
        return redisUtils.hasKey(RedisConstants.AUTO_CLASS_USER + key);
    }

    /**
     * 添加用户自动归类缓存
     */
    public Boolean setAutoGroup(String key, String value) {
        return redisUtils.set(RedisConstants.AUTO_CLASS_USER + key, value);
    }

    /**
     * 根据key和field获取异步任务（缓存优先，缓存未命中则查询数据库）
     */
    public EcmAsyncTask getEcmAsyncTask(String key, String fileId) {
        EcmAsyncTask asyncTask = (EcmAsyncTask) redisUtils.hget(key, fileId);

        if (asyncTask == null) {

            asyncTask = asyncTaskMapper.selectOne(new LambdaQueryWrapper<EcmAsyncTask>()
                    .eq(EcmAsyncTask::getBusiId, key.split(":")[2])
                    .eq(EcmAsyncTask::getFileId, fileId));

            if (asyncTask != null) {
                redisUtils.hset(key, fileId, asyncTask, TimeOutConstants.THERR_MONTH);
            }
        }

        return asyncTask;
    }

    /**
     * 获取业务下的所有自动归类待处理清单
     */
    public Map<Object, Object> getAllAutoGroup(String key) {
        return redisUtils.hgetall(key);
    }

    /**
     * 将需要推送的信息设入缓存
     */
    public Boolean setNeedPushBusiSync(String key, String value, Long time) {
        return redisUtils.set(key, value, time);
    }

    /**
     * DOC配置缓存
     */
    public Boolean setDocInfo(EcmDocDef ecmDocDef, Long time) {
        //动态树无资料节点
        if (ObjectUtils.isEmpty(ecmDocDef)) {
            return false;
        }
        String docCode = ecmDocDef.getDocCode();
        String key = RedisConstants.INTELLIGENT_PROCESSSING_DOC_DEF;
        return redisUtils.hset(key, docCode, ecmDocDef, time);
    }

    /**
     * 删除用户自动归类缓存
     */
    public void delAutoGroup(String key) {
        redisUtils.del(RedisConstants.AUTO_CLASS_USER + key);
    }

    /**
     * 删除自动归类待处理列表
     */
    public void delAutoClassPendingTaskList(String key, String field) {
        redisUtils.hdel(key, field);
    }

    /**
     * 添加自动归类待处理列表
     */
    public void setAutoClassPendingTaskList(String key, String field, List<EcmBusiDocRedisDTO> ecmBusiDocRedisDTOS) {
        redisUtils.hset(key, field, ecmBusiDocRedisDTOS);
    }

    /**
     * 全量更新资料缓存
     */
    public Map<String, Object> setDocInfoAll() {
        String key = RedisConstants.INTELLIGENT_PROCESSSING_DOC_DEF;
        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectList(null);
        Map<String, EcmDocDef> groupedByDocCode = ecmDocDefs.stream()
                .collect(Collectors.toMap(
                        EcmDocDef::getDocCode,
                        ecmDocDef -> ecmDocDef,
                        (existing, replacement) -> existing
                ));
        Map<String, Object> resultMap = new HashMap<>(groupedByDocCode);
        redisUtils.hmset(key, resultMap, TimeOutConstants.SEVEN_DAY);
        return resultMap;
    }

    /**
     * 获取全量的缓存配置
     */
    public Map<String, EcmDocDef> getDocInfoAll() {
        String key = RedisConstants.INTELLIGENT_PROCESSSING_DOC_DEF;

        // 1. 判断 Redis 中是否存在该大 Key
        boolean keyExists = redisUtils.hasKey(key);
        if (!keyExists) {
            // 2. 若不存在，先调用 setDocInfoAll() 初始化缓存
            setDocInfoAll();
        }

        // 3. 从 Redis 中获取所有数据并转换类型
        Map<Object, Object> allDocDefMap = redisUtils.hgetall(key);
        Map<String, EcmDocDef> resultMap = allDocDefMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.getKey(),
                        entry -> (EcmDocDef) entry.getValue()
                ));

        return resultMap;
    }

    /**
     * 获取智能化资料配置
     */
    public EcmDocDef getIntelligentProcessingEcmDocDef(String docCode) {
        String key = RedisConstants.INTELLIGENT_PROCESSSING_DOC_DEF;
        Boolean b = redisUtils.hasKey(key);
        EcmDocDef docDef = null;
        if (!b) {
            Map<String, Object> resultMap = setDocInfoAll();
            docDef = (EcmDocDef) resultMap.get(docCode);
        } else {
            docDef = (EcmDocDef) redisUtils.hget(key, docCode);
            if (ObjectUtils.isEmpty(docDef)) {
                docDef = ecmDocDefMapper.selectOne(new LambdaQueryWrapper<EcmDocDef>().eq(EcmDocDef::getDocCode, docCode));
                setDocInfo(docDef, TimeOutConstants.SEVEN_DAY);
            }
        }
        return docDef;
    }

    private void addEquipmentId(EcmBusiInfo ecmBusiInfo, EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(ecmBusiInfo.getAppCode());
        if (ecmAppDef != null) {
            ecmBusiInfoRedisDTO.setEquipmentId(ecmAppDef.getEquipmentId());
        }
    }

    public void addDocTypeTreeToEcmBusiDoc(EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        if (IcmsConstants.DYNAMIC_TREE.equals(ecmBusiInfoRedisDTO.getTreeType())) {
            //业务对应的资料静态树
            //获取业务关联的资料树列表
            List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(new QueryWrapper<EcmBusiDoc>()
                    .eq("doc_mark", IcmsConstants.ZERO)
                    .eq("busi_id", ecmBusiInfoRedisDTO.getBusiId()));
            EcmBusiDocRedisDTO dto = new EcmBusiDocRedisDTO();
            dto.setDocId(ecmBusiInfoRedisDTO.getBusiId());
            ecmBusiDocs.forEach(s -> {
                if (s.getParentId() == null) {
                    s.setParentId(ecmBusiInfoRedisDTO.getBusiId());
                }
            });
            commonService.buildDynTree(dto, ecmBusiDocs);
            ecmBusiInfoRedisDTO.setEcmBusiDocRedisDTOS(dto.getChildren());
        }
    }


    private void addEcmBusiDocs(EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO) {
        //获取标记节点列表
        List<EcmBusiDoc> ecmBusiDocs = ecmBusiDocMapper.selectList(new QueryWrapper<EcmBusiDoc>()
                .eq("doc_mark", IcmsConstants.DOC_MARK_MARK)
                .eq("busi_id", ecmBusiInfoRedisDTO.getBusiId()));
        ecmBusiInfoRedisDTO.setEcmBusiDocs(ecmBusiDocs);
    }


    private void getDocCodeByReidsTreeDto(List<EcmBusiDocRedisDTO> data, List<String> doc) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        for (EcmBusiDocRedisDTO dto : data) {
            doc.add(dto.getDocCode());
            if (!CollectionUtils.isEmpty(dto.getChildren())) {
                getDocCodeByReidsTreeDto(dto.getChildren(), doc);
            }
        }
    }

    public void getAllTaskType(List<FileInfoRedisDTO> fileInfoRedisEntities, Long busiId) {
        String key = RedisConstants.BUSIASYNC_TASK_PREFIX + busiId;
        for (FileInfoRedisDTO fileInfoRedisEntity : fileInfoRedisEntities) {
            Long fileId = fileInfoRedisEntity.getFileId();
            EcmAsyncTask ecmAsyncTask = (EcmAsyncTask) redisUtils.hget(key,String.valueOf(fileInfoRedisEntity.getFileId()));
            if (ecmAsyncTask != null) {
                fileInfoRedisEntity.setTaskType(ecmAsyncTask.getTaskType());
            }else {
                LambdaQueryWrapper<EcmAsyncTask> lambdaQueryWrapper=new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(EcmAsyncTask::getBusiId,busiId);
                lambdaQueryWrapper.eq(EcmAsyncTask::getFileId,fileId);
                EcmAsyncTask asyncTask = asyncTaskMapper.selectOne(lambdaQueryWrapper);
                if (asyncTask != null){
                    fileInfoRedisEntity.setTaskType(asyncTask.getTaskType());
                }
            }
        }
    }

    public List<Long> getAllFileIds(Long buisId){
        ArrayList<Long> fileIds = new ArrayList<>();
        Map<Object, Object> hmget = redisUtils.hmget(RedisConstants.BUSIFILE_PREFIX + buisId);
        if (hmget != null) {
            for (Object object : hmget.keySet()) {
                String object2 = (String) object;
                fileIds.add(Long.valueOf(object2));
            }
        }else {
            List<EcmFileInfo> fileInfoList = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>()
                    .eq(EcmFileInfo::getBusiId, buisId));
            List<Long> fileIdsList = fileInfoList.stream()
                    .map(EcmFileInfo::getFileId)
                    .collect(Collectors.toList());
            fileIds.addAll(fileIdsList);
        }
        return fileIds;
    }

    public List<String> filterExistingFileIds(List<Long> fileIds, Long busiId) {
        String key = RedisConstants.BUSIASYNC_TASK_PREFIX + busiId;
        ArrayList<String> fileIdList = new ArrayList<>();
        for (Long fileId : fileIds) {
            EcmAsyncTask ecmAsyncTask = (EcmAsyncTask) redisUtils.hget(key,String.valueOf(fileId));
            if (ecmAsyncTask == null) {
                continue;
            }
            String taskType = ecmAsyncTask.getTaskType();
            if (taskType.charAt(IcmsConstants.TYPE_FOUR - 1) == EcmCheckAsyncTaskEnum.INITIAL_STATE
                    .description().charAt(0) && taskType.charAt(IcmsConstants.TYPE_TEN - 1) == EcmCheckAsyncTaskEnum.INITIAL_STATE
                    .description().charAt(0)){
                continue;
            }
            fileIdList.add(String.valueOf(fileId));
        }
        return fileIdList;
    }

    /**
     * url一次有效缓存设置,60秒有效
     */
    public void setUrlOnceNonce(String nonce, String flagId) {
        String key = RedisConstants.ONCE_URL_NONCE + nonce;
        redisUtils.set(key, flagId, TimeOutConstants.ONE_SECONDS);
    }

    /**
     * 获取url一次有效缓存设置
     */
    public String getUrlOnceNonce(String nonce) {
        String key = RedisConstants.ONCE_URL_NONCE + nonce;
        return redisUtils.get(key);
    }

    /**
     * 清有效缓存设置
     */
    public void delUrlOnceNonce(String nonce) {
        String key = RedisConstants.ONCE_URL_NONCE + nonce;
        redisUtils.del(key);
    }
}
