package com.sunyard.ecm.service;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.StaticTreePermissService;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiMetadataMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmFileExpireInfoMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmFileExpireInfo;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.vo.FileExpireVO;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author ypy
 * @since 2025/11/5
 * @desc 影像期限接口
 */
@Slf4j
@Service
public class OperateExpireService {
    @Value("${fileIndex:ecm_file_dev}")
    private String fileIndex;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private StaticTreePermissService staticTreePermissService;
    @Resource
    private EcmFileExpireInfoMapper ecmFileExpireInfoMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;

    /**
     * 设置到期实间
     */
    @WebsocketNoticeAnnotation(busiId = "#vo.busiId")
    public void setFileExpireDate(FileExpireVO vo) {
        AssertUtils.isNull(vo,"参数为空");
        AssertUtils.isNull(vo.getBusiId(),"业务参数为空");
        AssertUtils.isNull(vo.getFileIds(),"文件参数为空");
//        AssertUtils.isNull(vo.getExpireDate(),"失效时间参数为空");
        if(!ObjectUtils.isEmpty(vo.getExpireDate())){
            AssertUtils.isTrue(vo.getExpireDate().before(new Date()),"失效时间参数不合法");
        }else {
            //删除
            delete(vo);
        }

        Date expireDate = vo.getExpireDate();
        List<Long> fileIds = vo.getFileIds();
        //批量查询数据库中已存在的记录
        List<EcmFileExpireInfo> existList = ecmFileExpireInfoMapper.selectList(
                new LambdaQueryWrapper<EcmFileExpireInfo>()
                        .in(EcmFileExpireInfo::getFileId, fileIds)
        );
        //  将已存在记录转为 Map：key=fileId，value= id
        Map<Long, Long> fileIdToIdMap = existList.stream()
                .collect(Collectors.toMap(
                        EcmFileExpireInfo::getFileId,
                        EcmFileExpireInfo::getId
                ));
        // 拆分列表：新增列表和更新列表
        List<EcmFileExpireInfo> insertList = new ArrayList<>();
        List<EcmFileExpireInfo> updateList = new ArrayList<>();
        for (Long fileId : fileIds) {
            EcmFileExpireInfo info = new EcmFileExpireInfo();
            info.setFileId(fileId);
            info.setExpireDate(expireDate);
            info.setIsExpired(IcmsConstants.ZERO);
            // 判断是否存在：通过 fileIdToIdMap 检查
            if (fileIdToIdMap.containsKey(fileId)) {
                Long existId = fileIdToIdMap.get(fileId);
                info.setId(existId);
                updateList.add(info);
            } else {
                insertList.add(info);
            }
        }
        saveInfos(vo, expireDate, fileIds, insertList,updateList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(FileExpireVO vo) {
        ecmFileExpireInfoMapper.deleteBatchIds(vo.getFileIds());
    }

    /**
     * 保存信息
     * @param vo
     * @param expireDate
     * @param fileIds
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveInfos(FileExpireVO vo, Date expireDate, List<Long> fileIds, List<EcmFileExpireInfo> insertInfos,List<EcmFileExpireInfo> updateInfos) {
        if(!CollectionUtils.isEmpty(insertInfos)){
            //插入实体数据
            MybatisBatch<EcmFileExpireInfo> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, insertInfos);
            MybatisBatch.Method<EcmFileExpireInfo> method = new MybatisBatch.Method<>(EcmFileExpireInfoMapper.class);
            mybatisBatch.execute(method.insert());
        }
        if(!CollectionUtils.isEmpty(updateInfos)){
            //更新实体数据
            MybatisBatch<EcmFileExpireInfo> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, updateInfos);
            MybatisBatch.Method<EcmFileExpireInfo> method = new MybatisBatch.Method<>(EcmFileExpireInfoMapper.class);
            mybatisBatch.execute(method.updateById());
        }
        //写入redis缓存
        List<FileInfoRedisDTO> fileInfoRedis = busiCacheService.getFileInfoRedis(vo.getBusiId(), fileIds);
        fileInfoRedis.forEach(info->{
            info.setExpireDate(expireDate);
            info.setIsExpired(IcmsConstants.ZERO);
        });
        busiCacheService.updateFileInfoRedis(fileInfoRedis);
    }

    /**
     * 查询到期文件
     * @param vo
     * @return
     */
    public PageInfo searchExpireInfos(FileExpireVO vo, AccountTokenExtendDTO tokenExtendDTO){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+8")); // 建议显式设置时区，防止服务器时区不一致
        Date startDate = null;
        Date endDate = null;

        try {
            // 2. 安全解析开始时间
            if (!ObjectUtils.isEmpty(vo.getExpireDateStart())) {
                startDate = sdf.parse(vo.getExpireDateStart());
            }

            // 3. 安全解析结束时间
            if (!ObjectUtils.isEmpty(vo.getExpireDateEnd())) {
                endDate = sdf.parse(vo.getExpireDateEnd());
            }
        } catch (Exception e) {
            // 如果格式不对，抛出友 好的业务异常，而不是让系统报 SQL 错误
            throw new IllegalArgumentException("日期格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式", e);
        }
        // 1. 获取当前用户token对应的有权限的appCode集合
        Set<String> permissionAppCodes = staticTreePermissService.getAppCodeHaveByToken(
                null,
                tokenExtendDTO,
                "read"
        );
        // 若权限集合为空，直接返回空结果（无权限访问任何数据）
        if (CollectionUtils.isEmpty(permissionAppCodes)) {
            PageInfo pageInfo = new PageInfo<>();
            pageInfo.setTotal(0L);
            pageInfo.setList(new ArrayList<>());
            return pageInfo;
        }
        //根据条件拿出全部的失效数据在进行筛选(1 代表到期)
        List<EcmFileExpireInfo> ecmFileExpireInfos = ecmFileExpireInfoMapper.selectList(
                new LambdaQueryWrapper<EcmFileExpireInfo>().eq(EcmFileExpireInfo::getIsExpired,IcmsConstants.ONE)
                        .between(!ObjectUtils.isEmpty(vo.getExpireDateStart()) && !ObjectUtils.isEmpty(vo.getExpireDateEnd()),
                                EcmFileExpireInfo::getExpireDate, startDate, endDate)
                        .orderByDesc(EcmFileExpireInfo::getCreateTime)
        );
        if(CollectionUtils.isEmpty(ecmFileExpireInfos)){
            return new PageInfo();
        }
        Map<Long, Date> dateMap = ecmFileExpireInfos.stream()
                .collect(Collectors.toMap(
                        EcmFileExpireInfo::getFileId,
                        EcmFileExpireInfo::getExpireDate
                ));
        List<Long> fileIds = ecmFileExpireInfos.stream().map(EcmFileExpireInfo::getFileId).collect(Collectors.toList());
        // 业务属性条件检索
        List<Long> filterBusiIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(vo.getAttrList())) {
            // 验证权限内的业务类型非空
            List<EcmAppAttrDTO> filterAttr = vo.getAttrList().stream()
                    .filter(p -> !ObjectUtils.isEmpty(p.getAppAttrValue()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(filterAttr)) {
                // 使用权限内的appCode查询busiIds
                filterBusiIds = ecmBusiMetadataMapper.complexSelect(filterAttr, filterAttr.size(),
                        new ArrayList<>(permissionAppCodes));
                filterBusiIds.add(-Long.MAX_VALUE);
            }
        }
         // 2. 处理业务类型ID：仅保留权限内的appCode及子ID
        List<String> appCodeList;
        // 原传入的appCodeList与权限集合取交集
        if (!CollectionUtils.isEmpty(vo.getAppCode())) {
            appCodeList = vo.getAppCode().stream()
                    .filter(permissionAppCodes::contains)
                    .collect(Collectors.toList());
        } else {
            appCodeList = new ArrayList<>();
        }

        // 获取子ID并过滤权限
        List<String> childIds = new ArrayList<>();
        getAllChildAppTypeIds(childIds, appCodeList);
        // 子ID也需过滤，仅保留权限内的
        childIds = childIds.stream()
                .filter(permissionAppCodes::contains)
                .collect(Collectors.toList());
        appCodeList.addAll(childIds);
        // 若最终appCodeList为空,则展示所有权限下的数据
        if (CollectionUtils.isEmpty(appCodeList)) {
            appCodeList.addAll(new ArrayList<>(permissionAppCodes));
        }
        List<EcmFileInfo> infos = ecmFileInfoMapper.selectList(new LambdaQueryWrapper<EcmFileInfo>()
                .like(StringUtils.hasText(vo.getCreateUser()),EcmFileInfo::getCreateUserName,vo.getCreateUser())
                .in(!ObjectUtils.isEmpty(vo.getDocCode()),EcmFileInfo::getDocCode, vo.getDocCode())
                .in(!CollectionUtils.isEmpty(filterBusiIds),EcmFileInfo::getBusiId, filterBusiIds)
                .in(EcmFileInfo::getFileId, fileIds));
        if(CollectionUtils.isEmpty(infos)){
            return new PageInfo();
        }
        Set<Long> busiIds = infos.stream().map(EcmFileInfo::getBusiId).collect(Collectors.toSet());
        List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(new LambdaQueryWrapper<EcmBusiInfo>()
                .eq(!ObjectUtils.isEmpty(vo.getBusiNo()),EcmBusiInfo::getBusiNo, vo.getBusiNo())
                .in(!ObjectUtils.isEmpty(appCodeList),EcmBusiInfo::getAppCode, appCodeList)
                .in(EcmBusiInfo::getBusiId, busiIds));
        if(CollectionUtils.isEmpty(ecmBusiInfos)){
            return new PageInfo();
        }
        //需要过滤的busiId
        List<Long> busiIdList = ecmBusiInfos.stream().map(EcmBusiInfo::getBusiId).collect(Collectors.toList());
        Set<String> appCodes = ecmBusiInfos.stream().map(EcmBusiInfo::getAppCode).collect(Collectors.toSet());
        // 建立 busiId 与 appCode 的映射
        Map<Long, List<EcmBusiInfo>> busiInfosMap = ecmBusiInfos.stream()
                .collect(Collectors.groupingBy(EcmBusiInfo::getBusiId));

        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appCodes);
        Map<String, List<EcmAppDef>> groupedByApp = ecmAppDefs.stream()
                .collect(Collectors.groupingBy(EcmAppDef::getAppCode));
        List<FileInfoRedisDTO> ecmFileInfoDTOS = PageCopyListUtils.copyListProperties(infos, FileInfoRedisDTO.class);
        ecmFileInfoDTOS = ecmFileInfoDTOS.stream().filter(s -> busiIdList.contains(s.getBusiId())).collect(Collectors.toList());
        /*//判断有没有传dtdCode,传和不传为两种查询方式
        Map<String, String> fileIdToDtdNamesMap = new HashMap<>();
        List<EsEcmFile> esEcmFiles = esEcmFileMapper
                .selectList(new LambdaEsQueryWrapper<EsEcmFile>().indexName(fileIndex)
                        .match(!ObjectUtils.isEmpty(vo.getDtdCode()), EsEcmFile::getDtdCode, vo.getDtdCode())
                        .in(!fileIds.isEmpty(), EsEcmFile::getFileId, fileIds));
        if (!CollectionUtils.isEmpty(esEcmFiles)) {
            List<String> ids = esEcmFiles.stream().map(EsEcmFile::getFileId).collect(Collectors.toList());
            fileIdToDtdNamesMap = esEcmFiles.stream()
                    .collect(Collectors.toMap(
                            EsEcmFile::getFileId,
                            ecmFile -> Optional.ofNullable(ecmFile.getDtdTypeName()).orElse("")
                    ));
            ecmFileInfoDTOS = ecmFileInfoDTOS.stream().filter(s -> ids.contains(s.getFileId().toString())).collect(Collectors.toList());
        }*/
        //处理资料名称
        List<String> docList = ecmFileInfoDTOS.stream().map(FileInfoRedisDTO::getDocCode).collect(Collectors.toList());
        List<EcmDocDef> ecmDocDefList = ecmDocDefMapper.selectBatchIds(docList);
        Map<String, List<EcmDocDef>> docMap = ecmDocDefList.stream().collect(Collectors.groupingBy(EcmDocDef::getDocCode));
        //处理数据
        for(FileInfoRedisDTO infoDTO : ecmFileInfoDTOS){
//            String dtd = fileIdToDtdNamesMap.get(infoDTO.getFileId().toString());
            EcmBusiInfo busiInfo = busiInfosMap.get(infoDTO.getBusiId()).get(0);
            List<EcmAppDef> appDefs = groupedByApp.get(busiInfo.getAppCode());
//            infoDTO.setDtdTypeName(dtd);
            infoDTO.setAppTypeName(appDefs.get(0).getAppName());
            infoDTO.setExpireDate(dateMap.get(infoDTO.getFileId()));
            infoDTO.setBusiNo(busiInfo.getBusiNo());
            infoDTO.setStatus(busiInfo.getStatus());
            if (IcmsConstants.UNCLASSIFIED_ID.equals(infoDTO.getDocCode())) {
                infoDTO.setDocName(IcmsConstants.UNCLASSIFIED);
            }else {
                infoDTO.setDocName(docMap.get(infoDTO.getDocCode()).get(0).getDocName());
            }

        }
        ecmFileInfoDTOS = ecmFileInfoDTOS.stream()
                .sorted(Comparator.comparing(FileInfoRedisDTO::getExpireDate).reversed()).collect(Collectors.toList());
        //手动分页
        int total = ecmFileInfoDTOS.size();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(vo.getPageSize());
        pageInfo.setPageNum(vo.getPageNum());
        int startIndex = (vo.getPageNum() - 1) * vo.getPageSize();
        int endIndex = Math.min(startIndex + vo.getPageSize(), total);
        pageInfo.setTotal(total);
        ecmFileInfoDTOS = ecmFileInfoDTOS.subList(startIndex, endIndex);
        pageInfo.setList(ecmFileInfoDTOS);
        return pageInfo;
    }


     private void getAllChildAppTypeIds(List<String> ids, List<String> appTypeIds) {
        if (CollectionUtils.isEmpty(appTypeIds)) {
            return;
        }
        for (String a : appTypeIds) {
            LambdaQueryWrapper<EcmAppDef> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EcmAppDef::getParent, a);
            List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(wrapper);
            if (ecmAppDefs.size() == IcmsConstants.ZERO) {
                ids.add(a);
            } else {
                List<String> list = new ArrayList<>();
                ecmAppDefs.forEach(e -> {
                    list.add(e.getAppCode());
                });
                getAllChildAppTypeIds(ids, list);
            }
        }
    }

    /**
     * 文件到期任务
     */
    public void updateExpireStatus(){
        //查找失效文件
        List<EcmFileExpireInfo> ecmFileExpireInfos = ecmFileExpireInfoMapper.selectList(new LambdaQueryWrapper<EcmFileExpireInfo>()
                .lt(EcmFileExpireInfo::getExpireDate, new Date()));
        if(CollectionUtils.isEmpty(ecmFileExpireInfos)){
            return;
        }
        List<Long> fileIds = ecmFileExpireInfos.stream().map(EcmFileExpireInfo::getFileId).collect(Collectors.toList());
        List<EcmFileInfo> ecmFileInfos = ecmFileInfoMapper.selectList(
                new LambdaQueryWrapper<EcmFileInfo>().in(EcmFileInfo::getFileId, fileIds));
        //更新db和redis
        updateInfos(ecmFileExpireInfos, ecmFileInfos);

    }

    @Transactional(rollbackFor = Exception.class)
    public void updateInfos(List<EcmFileExpireInfo> ecmFileExpireInfos, List<EcmFileInfo> ecmFileInfos) {
        //更新db
        ecmFileExpireInfos.forEach(s->s.setIsExpired(IcmsConstants.ONE));
        MybatisBatch<EcmFileExpireInfo> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmFileExpireInfos);
        MybatisBatch.Method<EcmFileExpireInfo> method = new MybatisBatch.Method<>(EcmFileExpireInfoMapper.class);
        mybatisBatch.execute(method.updateById());
        //更新redis
        for(EcmFileInfo e : ecmFileInfos){
            //更改到期状态
            FileInfoRedisDTO fileInfoRedisSingle = busiCacheService.getFileInfoRedisSingle(e.getBusiId(), e.getFileId());
            fileInfoRedisSingle.setIsExpired(IcmsConstants.ONE);
            busiCacheService.updateFileInfoRedis(fileInfoRedisSingle);
        }
    }
}
