package com.sunyard.ecm.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.BusiLogConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiRecycleDelDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiRecycleInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiRecycleRestoreDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.es.EsEcmBusi;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmBusiMetadataMapper;
import com.sunyard.ecm.mapper.EcmBusiRecycleMapper;
import com.sunyard.ecm.mapper.EcmDestroyListMapper;
import com.sunyard.ecm.mapper.EcmDestroyTaskMapper;
import com.sunyard.ecm.mapper.EcmFileBatchOperationMapper;
import com.sunyard.ecm.mapper.EcmFileCommentMapper;
import com.sunyard.ecm.mapper.EcmFileHistoryMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.EcmFileMarkCommentMapper;
import com.sunyard.ecm.mapper.es.EsEcmBusiMapper;
import com.sunyard.ecm.mapper.es.EsEcmFileMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiRecycle;
import com.sunyard.ecm.po.EcmDestroyList;
import com.sunyard.ecm.po.EcmDestroyTask;
import com.sunyard.ecm.po.EcmFileBatchOperation;
import com.sunyard.ecm.po.EcmFileComment;
import com.sunyard.ecm.po.EcmFileHistory;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.po.EcmFileMarkComment;
import com.sunyard.ecm.vo.EcmBusiRecycleSearchVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.dromara.easyes.core.conditions.update.LambdaEsUpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 影像业务回收站
 *
 * @author wzz
 * @DESC:回收站
 * @since 2024-6-6
 */
@Slf4j
@Service
public class OperateRecycleService {
    @Value("${bizIndex:ecm_busi_dev}")
    private String bizIndex;
    @Value("${fileIndex:ecm_file_dev}")
    private String fileIndex;
    @Resource
    private SnowflakeUtils snowflakeUtils;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EsEcmBusiMapper esEcmBusiMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmFileHistoryMapper ecmFileHistoryMapper;
    @Resource
    private EcmFileCommentMapper ecmFileCommentMapper;
    @Resource
    private EcmFileBatchOperationMapper ecmFileBatchOperationMapper;
    @Resource
    private EcmFileMarkCommentMapper ecmFileMarkCommentMapper;
    @Resource
    private EsEcmFileMapper esEcmFileMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmBusiMetadataMapper ecmBusiMetadataMapper;
    @Resource
    private EcmBusiRecycleMapper ecmBusiRecycleMapper;
    @Resource
    private LogBusiService logBusiService;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EcmDestroyTaskMapper ecmDestroyTaskMapper;
    @Resource
    private EcmDestroyListMapper ecmDestroyListMapper;

    /**
     * 影像业务回收站列表
     */
    public Result search(EcmBusiRecycleSearchVO ecmBusiRecycleSearchVO, AccountTokenExtendDTO token) {
        List<Long> busiIds = new LinkedList<>();
        // 业务属性条件检索
        if (CollUtil.isNotEmpty(ecmBusiRecycleSearchVO.getAttrList())) {
            Assert.notEmpty(ecmBusiRecycleSearchVO.getAppCodes(), "业务类型不能为空");
            List<EcmAppAttrDTO> filterAttrList = ecmBusiRecycleSearchVO.getAttrList().stream()
                    .filter(p -> !ObjectUtils.isEmpty(p.getAppAttrValue()))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(filterAttrList)) {
                busiIds = ecmBusiMetadataMapper.complexSelect(filterAttrList, filterAttrList.size(), ecmBusiRecycleSearchVO.getAppCodes());
                busiIds.add(-Long.MAX_VALUE);
            }
        }
        // 若传入的业务类型Code为父code，获取所有子code
        List<String> appTypeCodes = new ArrayList<>();
        List<String> appCodeList = ecmBusiRecycleSearchVO.getAppCodes();
        if (CollUtil.isNotEmpty(appCodeList)) {
            getAllChildAppTypeIds(appTypeCodes, appCodeList);
            appCodeList.addAll(appTypeCodes);
        }
        PageHelper.startPage(ecmBusiRecycleSearchVO.getPageNum(), ecmBusiRecycleSearchVO.getPageSize());
        List<EcmBusiRecycleInfoDTO> ecmBusiRecycleInfoDTOList =
                ecmBusiRecycleMapper.selectBusiRecycleList(ecmBusiRecycleSearchVO, appCodeList, busiIds);
        // 填充业务信息
        handleToStr(ecmBusiRecycleInfoDTOList);

        return Result.success(new PageInfo<>(ecmBusiRecycleInfoDTOList));
    }

    /**
     * 根据父编码获取所有子级编码
     */
    private void getAllChildAppTypeIds(List<String> ids, List<String> appTypeIds) {
        if (CollUtil.isEmpty(appTypeIds)) {
            return;
        }
        for (String appCode : appTypeIds) {
            LambdaQueryWrapper<EcmAppDef> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EcmAppDef::getParent, appCode);
            List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectList(wrapper);
            if (ecmAppDefs.size() == IcmsConstants.ZERO) {
                ids.add(appCode);
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
     * 业务及回收信息填充
     */
    private void handleToStr(List<EcmBusiRecycleInfoDTO> ecmBusiRecycleInfoDTOList) {
        if (CollUtil.isEmpty(ecmBusiRecycleInfoDTOList)) {
            return;
        }
        Set<String> appCodeSet = ecmBusiRecycleInfoDTOList.stream()
                .map(EcmBusiRecycleInfoDTO::getAppCode)
                .collect(Collectors.toSet());
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appCodeSet);
        Map<String, String> appCodeAndNameMap = ecmAppDefs.stream()
                .collect(Collectors.toMap(EcmAppDef::getAppCode, EcmAppDef::getAppName));
        for (EcmBusiRecycleInfoDTO busiRecycleInfoDTO : ecmBusiRecycleInfoDTOList) {
            // 业务类型名称
            busiRecycleInfoDTO.setAppTypeName(appCodeAndNameMap.get(busiRecycleInfoDTO.getAppCode()));
            if (busiRecycleInfoDTO.getRightVer() == 0) {
                busiRecycleInfoDTO.setRightVer(null);
            }
        }
    }

    /**
     * 业务回收
     */
    public Result add(EcmBusiInfo ecmBusiInfo, AccountTokenExtendDTO tokenExtendDTO) {
        Assert.notNull(ecmBusiInfo.getBusiId(), "业务ID不能为空!");
        DateTime date = DateUtil.date();
        EcmBusiRecycle ecmBusiRecycle = new EcmBusiRecycle();
        ecmBusiRecycle.setBusiId(ecmBusiInfo.getBusiId())
                .setBusiNo(ecmBusiInfo.getBusiNo())
                .setRecycleUser(tokenExtendDTO.getUsername())
                .setRecycleTime(date);
        ecmBusiRecycleMapper.insert(ecmBusiRecycle);
        // 添加日志
        logBusiService.addLog(ecmBusiRecycle.getBusiId(), "添加回收站:" + ecmBusiInfo.getBusiNo(), "", tokenExtendDTO, BusiLogConstants.OPERATION_TYPE_THREE);

        return Result.success();
    }

    /**
     * 业务批量回收
     */
    public Result addBatch(List<EcmBusiInfo> ecmBusiInfoList, AccountTokenExtendDTO tokenExtendDTO) {
        Assert.notEmpty(ecmBusiInfoList, "回收业务列表不能为空!");
        DateTime date = DateUtil.date();
        List<EcmBusiRecycle> ecmBusiRecycleList = new LinkedList<>();
        ecmBusiInfoList.forEach(ecmBusiInfo -> {
            EcmBusiRecycle ecmBusiRecycle = new EcmBusiRecycle();
            ecmBusiRecycle.setRecycleId(snowflakeUtils.nextId())
                    .setBusiId(ecmBusiInfo.getBusiId())
                    .setBusiNo(ecmBusiInfo.getBusiNo())
                    .setRecycleUser(tokenExtendDTO.getUsername())
                    .setRecycleTime(date)
                    .setCreateTime(date)
                    .setUpdateTime(date);
            ecmBusiRecycleList.add(ecmBusiRecycle);
        });
        // 批量添加

        insertEcmBusiRecycles(ecmBusiRecycleList);
        //ecmBusiRecycleMapper.insertBatch(ecmBusiRecycleList);

        return Result.success();
    }

    /**
     * 批量插入影像业务回收信息
     */
    private void insertEcmBusiRecycles(List<EcmBusiRecycle> ecmBusiRecycleList) {
        MybatisBatch<EcmBusiRecycle> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmBusiRecycleList);
        MybatisBatch.Method<EcmBusiRecycle> method = new MybatisBatch.Method<>(EcmBusiRecycleMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 根据业务回收ID删除-硬删除，不保留业务及回收站信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result del(EcmBusiRecycleDelDTO ecmBusiRecycleDelDTO, AccountTokenExtendDTO tokenExtendDTO) {
        Assert.notNull(ecmBusiRecycleDelDTO.getBusiId(), "业务Id不能为空!");
        Long recycleId = ecmBusiRecycleDelDTO.getRecycleId();
        recycleId = ObjectUtil.isNull(recycleId) ? snowflakeUtils.nextId() : recycleId;
        Long busiId = ecmBusiRecycleDelDTO.getBusiId();
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectByIdWithDeleted(busiId);
        // 添加日志
        logBusiService.addLog(busiId, "彻底删除业务:", "", tokenExtendDTO,BusiLogConstants.OPERATION_TYPE_THREE);
        //redis数据删除
        busiCacheService.delBusiInfo(busiId);
        busiCacheService.delFileInfoRedisReal(busiId);
        // 删除业务信息及回收站信息
        ecmBusiInfoMapper.deleteByBusiId(busiId);
        ecmBusiRecycleMapper.deleteById(recycleId);
        // 清除es数据
        esEcmBusiMapper.deleteById(busiId, bizIndex);
        //删除文件
        List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(busiId);
        List<EcmFileInfo> ecmFileInfos = PageCopyListUtils.copyListProperties(fileInfos, EcmFileInfo.class);
        if (!CollectionUtils.isEmpty(ecmFileInfos)) {
            List<Long> collect = ecmFileInfos.stream().map(EcmFileInfo::getFileId).collect(Collectors.toList());
            //更新es数据
            for (Long fileId : collect) {
                esEcmFileMapper.deleteById(fileId + "", fileIndex);
            }
            ecmFileHistoryMapper.delete(new LambdaQueryWrapper<EcmFileHistory>().eq(EcmFileHistory::getBusiId, busiId));
            ecmFileBatchOperationMapper.delete(new LambdaQueryWrapper<EcmFileBatchOperation>().in(EcmFileBatchOperation::getFileId, collect));
            ecmFileCommentMapper.delete(new LambdaQueryWrapper<EcmFileComment>().eq(EcmFileComment::getBusiId, busiId));
            ecmFileMarkCommentMapper.delete(new LambdaQueryWrapper<EcmFileMarkComment>().eq(EcmFileMarkComment::getBusiId, busiId));

        }
        //彻底删除添加一条销毁任务
        addEcmDestroyTask(tokenExtendDTO, ecmBusiInfo);
        return Result.success();
    }

    private void addEcmDestroyTask(AccountTokenExtendDTO tokenExtendDTO, EcmBusiInfo ecmBusiInfo) {
        EcmDestroyTask task = new EcmDestroyTask();
        long destroyId = snowflakeUtils.nextId();
        task.setId(destroyId);
        task.setDestroyType(IcmsConstants.DESTROY_TYPE_THREE);
        task.setAppCode(ecmBusiInfo.getAppCode());
        task.setBusiCreateDate("");
        task.setOrgCode(ecmBusiInfo.getOrgCode());
        task.setCreateUser(tokenExtendDTO.getUsername());
        task.setStatus(IcmsConstants.DESTROY_STATUS_ONE);
        task.setOrgName(tokenExtendDTO.getOrgName());
        task.setCreateUserName(tokenExtendDTO.getName());
        EcmDestroyList destroyList = new EcmDestroyList();
        BeanUtils.copyProperties(ecmBusiInfo, destroyList);
        destroyList.setId(snowflakeUtils.nextId());
        destroyList.setDestroyTaskId(destroyId);
        Long l = ecmFileInfoMapper.selectCount(new LambdaQueryWrapper<EcmFileInfo>()
                .eq(EcmFileInfo::getBusiId, destroyList.getBusiId()));
        destroyList.setFileCount(l);
        ecmDestroyTaskMapper.insert(task);
        ecmDestroyListMapper.insert(destroyList);
    }

    /**
     * 批量删除-硬删除，不保留业务及回收站信息
     */
    public Result delBatch(List<EcmBusiRecycleDelDTO> ecmBusiRecycleDelDTOList, AccountTokenExtendDTO tokenExtendDTO) {
        Assert.notEmpty(ecmBusiRecycleDelDTOList, "业务回收ID列表不能为空!");
        // 补充recycleId
        Map<Long, Long> recycleWithBusMap = ecmBusiRecycleDelDTOList.stream()
                .collect(Collectors.toMap(EcmBusiRecycleDelDTO::getBusiId, EcmBusiRecycleDelDTO::getRecycleId));
        //彻底删除添加一条销毁任务
        Set<Long> busiIds = recycleWithBusMap.keySet();
        for(Long busiId : busiIds){
            EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectByIdWithDeleted(busiId);
            addEcmDestroyTask(tokenExtendDTO, ecmBusiInfo);
        }
        // 批量删除业务信息及回收站信息
        ecmBusiInfoMapper.deleteBatchByBusiId(recycleWithBusMap.keySet());
        ecmBusiRecycleMapper.deleteBatchIds(recycleWithBusMap.values());
        // 批量删除es数据
        esEcmBusiMapper.deleteBatchIds(recycleWithBusMap.keySet(), bizIndex);

        return Result.success();
    }

    /**
     * 恢复-单笔业务
     */
    @Transactional(rollbackFor = Exception.class)
    public Result restore(EcmBusiRecycleRestoreDTO ecmBusiRecycleRestoreDTO, AccountTokenExtendDTO tokenExtendDTO) {
        Assert.notNull(ecmBusiRecycleRestoreDTO.getRecycleId(), "业务回收ID不能为空!");
        Assert.notNull(ecmBusiRecycleRestoreDTO.getBusiId(), "业务ID不能为空!");
        // 获取业务信息
        Long recycleId = ecmBusiRecycleRestoreDTO.getRecycleId();
        Long busiId = ecmBusiRecycleRestoreDTO.getBusiId();
        EcmBusiInfo ecmBusiInfo = ecmBusiInfoMapper.selectByIdWithDeleted(busiId);
        // 判断相同业务类型下主索引是否相同
        int splitIndex = ecmBusiInfo.getBusiNo().lastIndexOf("_");
        String originalBusiNo = ecmBusiInfo.getBusiNo().substring(0, splitIndex);
        Long count = ecmBusiInfoMapper.selectCount(new LambdaQueryWrapper<EcmBusiInfo>()
                .eq(EcmBusiInfo::getBusiNo, originalBusiNo)
                .eq(EcmBusiInfo::getAppCode, ecmBusiInfo.getAppCode()));
        // 存在相同业务索引
        if (count > 0) {
            return Result.error("已存在相同业务主索引，无法恢复!", 400);
        }
        // 恢复
        ecmBusiInfo.setBusiNo(originalBusiNo).setIsDeleted(StateConstants.NO);
        // 更新业务信息-业务主索引、删除状态
        ecmBusiInfoMapper.updateByIdWithNoDeleted(ecmBusiInfo);
        ecmBusiRecycleMapper.deleteById(recycleId);
        // 更新Es业务状态
        updateEsBusiDeleted(Arrays.asList(busiId), tokenExtendDTO);
        //更新redis
        EcmBusiInfoRedisDTO ecmBusiInfoRedisDTO1 = busiCacheService.getEcmBusiInfoRedisDTO(tokenExtendDTO, ecmBusiRecycleRestoreDTO.getBusiId());
        ecmBusiInfoRedisDTO1.setIsDeleted(StateConstants.NO);
        busiCacheService.saveAndUpate(ecmBusiInfoRedisDTO1);
        // 添加日志
        logBusiService.addLog(busiId, "恢复业务:" + ecmBusiInfo.getBusiNo(), "", tokenExtendDTO,BusiLogConstants.OPERATION_TYPE_ZERO);

        return Result.success();
    }

    /**
     * 批量恢复
     */
    @Transactional(rollbackFor = Exception.class)
    public Result restoreBatch(List<EcmBusiRecycleRestoreDTO> ecmBusiRecycleRestoreDTOList, AccountTokenExtendDTO tokenExtendDTO) {
        Assert.notEmpty(ecmBusiRecycleRestoreDTOList, "业务回收列表不能为空!");
        // 索引号是否重复
        long count = ecmBusiRecycleRestoreDTOList.stream().map(EcmBusiRecycleRestoreDTO::getBusiNo)
                .distinct()
                .count();
        Assert.isFalse(ecmBusiRecycleRestoreDTOList.size() != count, "业务索引号不能重复！");
        ecmBusiRecycleRestoreDTOList.forEach(ecmBusiRecycleRestoreDTO -> {
            restore(ecmBusiRecycleRestoreDTO, tokenExtendDTO);
        });

        return Result.success();
    }

    /**
     * 更新Es业务删除状态
     */
    private void updateEsBusiDeleted(List<Long> busiIds, AccountTokenExtendDTO tokenExtendDTO) {
        if (CollUtil.isEmpty(busiIds)) {
            return;
        }
        esEcmBusiMapper.update(null, new LambdaEsUpdateWrapper<EsEcmBusi>()
                .indexName(bizIndex)
                .set(EsEcmBusi::getIsDeleted, StateConstants.NOT_DELETE)
                .in(CollUtil.isNotEmpty(busiIds), EsEcmBusi::getBusiId, busiIds)
        );
    }

}
