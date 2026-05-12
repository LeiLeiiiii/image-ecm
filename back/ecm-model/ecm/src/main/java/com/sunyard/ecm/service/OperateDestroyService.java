package com.sunyard.ecm.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.BusiLogConstants;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.ecm.EcmDestroyListDTO;
import com.sunyard.ecm.dto.ecm.EcmDestroyTaskDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.mapper.EcmAppDefMapper;
import com.sunyard.ecm.mapper.EcmAsyncTaskMapper;
import com.sunyard.ecm.mapper.EcmBusiInfoMapper;
import com.sunyard.ecm.mapper.EcmDestroyListMapper;
import com.sunyard.ecm.mapper.EcmDestroyTaskMapper;
import com.sunyard.ecm.mapper.EcmDocDefMapper;
import com.sunyard.ecm.mapper.EcmFileBatchOperationMapper;
import com.sunyard.ecm.mapper.EcmFileCommentMapper;
import com.sunyard.ecm.mapper.EcmFileHistoryMapper;
import com.sunyard.ecm.mapper.EcmFileInfoMapper;
import com.sunyard.ecm.mapper.EcmFileLabelMapper;
import com.sunyard.ecm.mapper.EcmFileMarkCommentMapper;
import com.sunyard.ecm.mapper.SysBusiLogMapper;
import com.sunyard.ecm.mapper.es.EsEcmBusiMapper;
import com.sunyard.ecm.mapper.es.EsEcmFileMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.po.EcmBusiInfo;
import com.sunyard.ecm.po.EcmBusiLog;
import com.sunyard.ecm.po.EcmDestroyList;
import com.sunyard.ecm.po.EcmDestroyTask;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.po.EcmFileInfo;
import com.sunyard.ecm.vo.DestroyFilterRuleVO;
import com.sunyard.ecm.vo.DestroyInfoVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.framework.mybatis.util.SnowflakeUtils;
import com.sunyard.module.storage.api.FileStorageApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 影像销毁
 * @author ypy
 * @since 2025-7-2
 * @DESC:销毁
 */
@Slf4j
@Service
public class OperateDestroyService {
    /**
     * 销毁类型(0:历史业务销毁;1:历史资料销毁;2:已删除销毁;3:回收站业务删除;4:已删除节点彻底删除)
     */
    private static final int DESTROY_TYPE_ZERO = 0;
    private static final int DESTROY_TYPE_ONE = 1;
    private static final int DESTROY_TYPE_TWO = 2;
    private static final int DESTROY_TYPE_THREE = 3;
    private static final int DESTROY_TYPE_FOUR = 4;

    @Value("${fileIndex:ecm_file_dev}")
    private String fileIndex;
    @Value("${bizIndex:ecm_busi_dev}")
    private String busiIndex;
    @Resource
    private SnowflakeUtils snowflakeUtils;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private FileStorageApi fileStorageApi;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private EcmDestroyTaskMapper ecmDestroyTaskMapper;
    @Resource
    private EcmDestroyListMapper ecmDestroyListMapper;
    @Resource
    private EcmAppDefMapper ecmAppDefMapper;
    @Resource
    private EcmDocDefMapper ecmDocDefMapper;
    @Resource
    private EcmBusiInfoMapper ecmBusiInfoMapper;
    @Resource
    private EcmFileInfoMapper ecmFileInfoMapper;
    @Resource
    private EcmFileHistoryMapper ecmFileHistoryMapper;
    @Resource
    private EcmFileLabelMapper ecmFileLabelMapper;
    @Resource
    private EcmFileBatchOperationMapper ecmFileBatchOperationMapper;
    @Resource
    private EcmFileCommentMapper ecmFileCommentMapper;
    @Resource
    private EcmFileMarkCommentMapper ecmFileMarkCommentMapper;
    @Resource
    private EcmAsyncTaskMapper ecmAsyncTaskMapper;
    @Resource
    private SysBusiLogMapper ecmBusiLogMapper;
    @Resource
    private EsEcmFileMapper esEcmFileMapper;
    @Resource
    private EsEcmBusiMapper esEcmBusiMapper;
    /**
     * 查询销毁任务
     */
    public Result searchTask(DestroyInfoVO destroyInfoVO){
        AssertUtils.isNull(destroyInfoVO,"参数错误");
        LambdaQueryWrapper<EcmDestroyTask> queryWrapper = getQueryWrapper(destroyInfoVO);
        PageHelper.startPage(destroyInfoVO.getPageNum(), destroyInfoVO.getPageSize());
        List<EcmDestroyTask> ecmDestroyTasks = ecmDestroyTaskMapper.selectList(queryWrapper);
        PageInfo pageInfo = new PageInfo<>(ecmDestroyTasks);
        List<EcmDestroyTaskDTO> ecmDestroyTaskDTOS = PageCopyListUtils.copyListProperties(ecmDestroyTasks, EcmDestroyTaskDTO.class);
        // 填充业务信息
        handleInfo(ecmDestroyTaskDTOS);
        pageInfo.setList(ecmDestroyTaskDTOS);
        return Result.success(pageInfo);
    }

    /**
     * 生成销毁清单
     */
    public Result createDestroyList(DestroyInfoVO destroyInfoVO){
        //生成销毁清单
        List<EcmDestroyListDTO> ecmDestroyListDTOS = getEcmDestroyListDTOS(destroyInfoVO);
        //分页
        int total = ecmDestroyListDTOS.size();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setPageSize(destroyInfoVO.getPageSize());
        pageInfo.setPageNum(destroyInfoVO.getPageNum());
        int startIndex = (destroyInfoVO.getPageNum() - 1) * destroyInfoVO.getPageSize();
        int endIndex = Math.min(startIndex + destroyInfoVO.getPageSize(), total);
        pageInfo.setTotal(total);
        ecmDestroyListDTOS = ecmDestroyListDTOS.subList(startIndex, endIndex);
        pageInfo.setList(ecmDestroyListDTOS);
        return Result.success(pageInfo);
    }

    private List<EcmDestroyListDTO> getEcmDestroyListDTOS(DestroyInfoVO destroyInfoVO) {
        //检验参数
        checkVo(destroyInfoVO);
        Integer destroyType = destroyInfoVO.getDestroyType();
        List<EcmDestroyListDTO> ecmDestroyListDTOS=new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = null;
        Date endTime = null;
        try {
            if (destroyInfoVO.getCreateTimeStart() != null) {
                startTime = sdf.parse(destroyInfoVO.getCreateTimeStart());
            }
            if (destroyInfoVO.getCreateTimeEnd() != null) {
                endTime = sdf.parse(destroyInfoVO.getCreateTimeEnd());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("日期格式解析失败", e);
        }
        //0:业务销毁;1:资料销毁;2:已删除销毁
        if(IcmsConstants.DESTROY_TYPE_ZERO.equals(destroyType)){
            String appCode = destroyInfoVO.getAppCode();
            List<EcmBusiInfo> ecmBusiInfos = ecmBusiInfoMapper.selectList(new LambdaQueryWrapper<EcmBusiInfo>()
                    .eq(EcmBusiInfo::getAppCode, appCode)
                    .eq(EcmBusiInfo::getOrgCode, destroyInfoVO.getOrgCode())
                    .eq(!ObjectUtils.isEmpty(destroyInfoVO.getBusiNo()),EcmBusiInfo::getBusiNo,destroyInfoVO.getBusiNo())
                    .ge(EcmBusiInfo::getCreateTime, startTime)
                    .le(EcmBusiInfo::getCreateTime, endTime)
                    .orderByAsc(EcmBusiInfo::getCreateTime)
            );
            ecmDestroyListDTOS = PageCopyListUtils.copyListProperties(ecmBusiInfos, EcmDestroyListDTO.class);
        }else if(IcmsConstants.DESTROY_TYPE_ONE.equals(destroyType)){
            ecmDestroyListDTOS = ecmBusiInfoMapper.selectListForDestroy(destroyInfoVO.getCreateTimeStart(),
                                    destroyInfoVO.getCreateTimeEnd(),null,destroyInfoVO.getDocCode(),
                                    destroyInfoVO.getOrgCode(),destroyInfoVO.getBusiNo());
        }else if(IcmsConstants.DESTROY_TYPE_TWO.equals(destroyType)){
            ecmDestroyListDTOS = ecmBusiInfoMapper.selectListForDestroy(destroyInfoVO.getCreateTimeStart(),
                    destroyInfoVO.getCreateTimeEnd(), StateConstants.COMMON_ONE,null,
                    destroyInfoVO.getOrgCode(),destroyInfoVO.getBusiNo());
        }
        //填充信息
        ecmDestroyListDTOS = handleDestroyLists(ecmDestroyListDTOS, destroyInfoVO);
        return ecmDestroyListDTOS;
    }

    /**
     * 生成销毁任务
     */
    public Result createDestroyTask(DestroyInfoVO destroyInfoVO, AccountToken token){
        List<EcmDestroyListDTO> ecmDestroyListDTOS = getEcmDestroyListDTOS(destroyInfoVO);
        List<EcmDestroyList> ecmDestroyLists = PageCopyListUtils.copyListProperties(ecmDestroyListDTOS, EcmDestroyList.class);
        EcmDestroyTask task = new EcmDestroyTask();
        long destroyId = snowflakeUtils.nextId();
        task.setId(destroyId);
        task.setDestroyType(destroyInfoVO.getDestroyType());
        if(IcmsConstants.DESTROY_TYPE_ZERO.equals(destroyInfoVO.getDestroyType())){
            task.setAppCode(destroyInfoVO.getAppCode());
        }else if(IcmsConstants.DESTROY_TYPE_ONE.equals(destroyInfoVO.getDestroyType())){
            task.setDocCode(destroyInfoVO.getDocCode());
        }
        String createTimeStart = destroyInfoVO.getCreateTimeStart();
        String createTimeEnd = destroyInfoVO.getCreateTimeEnd();
        String startTime = createTimeStart.substring(0, 10);
        String endTime = createTimeEnd.substring(0, 10);
        task.setBusiCreateDate(startTime+"~"+endTime);
        task.setOrgCode(destroyInfoVO.getOrgCode());
        task.setCreateUser(token.getUsername());
        task.setStatus(IcmsConstants.DESTROY_STATUS_ZERO);
        task.setOrgName(token.getOrgName());
        task.setCreateUserName(token.getName());
        ecmDestroyLists.forEach(s->{
            s.setDestroyTaskId(destroyId);
            s.setId(snowflakeUtils.nextId());
        });
        //抽个方法事务
        ecmDestroyTaskMapper.insert(task);
        MybatisBatch<EcmDestroyList> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmDestroyLists);
        MybatisBatch.Method<EcmDestroyList> method = new MybatisBatch.Method<>(EcmDestroyListMapper.class);
        mybatisBatch.execute(method.insert());
        return Result.success();
    }

    /**
     * 审核销毁任务
     */
    public Result auditTask(DestroyInfoVO destroyInfoVO, AccountToken token){
        AssertUtils.isNull(destroyInfoVO.getTaskId(),"参数错误");
        Long taskId = destroyInfoVO.getTaskId();
        EcmDestroyTask task = ecmDestroyTaskMapper.selectById(taskId);
        EcmDestroyTaskDTO taskDTO = new EcmDestroyTaskDTO();
        BeanUtils.copyProperties(task,taskDTO);
        if(IcmsConstants.DESTROY_TYPE_ZERO.equals(taskDTO.getDestroyType())){
            EcmAppDef ecmAppDef = ecmAppDefMapper.selectById(taskDTO.getAppCode());
            AssertUtils.isNull(ecmAppDef,"参数错误");
            taskDTO.setBusiTypeStr("(" + taskDTO.getAppCode() + ")" +ecmAppDef.getAppName());
            taskDTO.setDestroyTypeStr(IcmsConstants.DESTROY_TYPE_ZERO_STR);
        }else if(IcmsConstants.DESTROY_TYPE_ONE.equals(taskDTO.getDestroyType())){
            EcmDocDef ecmDocDef = ecmDocDefMapper.selectById(taskDTO.getDocCode());
            AssertUtils.isNull(ecmDocDef,"参数错误");
            taskDTO.setBusiTypeStr("(" + taskDTO.getDocCode() + ")" +ecmDocDef.getDocName());
            taskDTO.setDestroyTypeStr(IcmsConstants.DESTROY_TYPE_ONE_STR);
        }else if(IcmsConstants.DESTROY_TYPE_TWO.equals(taskDTO.getDestroyType())){
            taskDTO.setDestroyTypeStr(IcmsConstants.DESTROY_TYPE_TWO_STR);
        }
        //处理状态信息
        getStatusStr(taskDTO);
        PageHelper.startPage(destroyInfoVO.getPageNum(), destroyInfoVO.getPageSize());
        List<EcmDestroyList> ecmDestroyLists = ecmDestroyListMapper.selectList(new LambdaQueryWrapper<EcmDestroyList>()
                .eq(EcmDestroyList::getDestroyTaskId,task.getId())
                .eq(!ObjectUtils.isEmpty(destroyInfoVO.getBusiNo()),EcmDestroyList::getBusiNo,destroyInfoVO.getBusiNo()));
        PageInfo pageInfo = new PageInfo<>(ecmDestroyLists);
        List<EcmDestroyListDTO> ecmDestroyListDTOS = PageCopyListUtils.copyListProperties(ecmDestroyLists, EcmDestroyListDTO.class);
        DestroyInfoVO vo = new DestroyInfoVO();
        vo.setDestroyType(taskDTO.getDestroyType());
        //处理数据
        ecmDestroyListDTOS = handleDestroyLists(ecmDestroyListDTOS, vo);
        pageInfo.setList(ecmDestroyListDTOS);
        Map<String, Object> map = new HashMap<>();
        map.put("list", pageInfo);
        map.put("task", taskDTO);
        return Result.success(map);
    }

    /**
     * 填充状态str
     * @param taskDTO
     */
    private void getStatusStr(EcmDestroyTaskDTO taskDTO) {
        switch (taskDTO.getStatus()) {
            case DESTROY_TYPE_ZERO:
                taskDTO.setStatusStr(IcmsConstants.DESTROY_STATUS_ZERO_STR);
                break;
            case DESTROY_TYPE_ONE:
                taskDTO.setStatusStr(IcmsConstants.DESTROY_STATUS_ONE_STR);
                taskDTO.setAuditOpinion(IcmsConstants.AUDIT_OPINION_YES);
                break;
            case DESTROY_TYPE_TWO:
                taskDTO.setStatusStr(IcmsConstants.DESTROY_STATUS_TWO_STR);
                taskDTO.setAuditOpinion(IcmsConstants.AUDIT_OPINION_NO);
                break;
            case DESTROY_TYPE_THREE:
                taskDTO.setStatusStr(IcmsConstants.DESTROY_STATUS_THREE_STR);
                taskDTO.setAuditOpinion(IcmsConstants.AUDIT_OPINION_YES);
                break;
        }
    }

    /**
     * 审核销毁任务
     */
    public Result audit(DestroyInfoVO destroyInfoVO, AccountToken token) {
        AssertUtils.isNull(destroyInfoVO.getTaskId(),"参数错误");
        AssertUtils.isNull(destroyInfoVO.getAuditOpinion(),"参数错误");
        EcmDestroyTask task = ecmDestroyTaskMapper.selectById(destroyInfoVO.getTaskId());
        task.setAuditOpinion(destroyInfoVO.getAuditOpinion());
        if (!ObjectUtils.isEmpty(destroyInfoVO.getAuditNote())) {
            task.setAuditNote(destroyInfoVO.getAuditNote());
        }
        task.setAuditUser(token.getUsername());
        task.setAuditUserName(token.getName());
        task.setAuditTime(new Date());
        //AuditOpinion审核意见(1:同意销毁,2:拒绝销毁)
        if(destroyInfoVO.getAuditOpinion() == 1){
            task.setStatus(IcmsConstants.DESTROY_STATUS_ONE);
        }else {
            task.setStatus(IcmsConstants.DESTROY_STATUS_TWO);
        }
        ecmDestroyTaskMapper.updateById(task);
        return Result.success();
    }

    /**
     * 审核销毁任务
     */
    public Result cancel(Long taskId) {
        AssertUtils.isNull(taskId,"参数错误");
        EcmDestroyTask task = ecmDestroyTaskMapper.selectById(taskId);
        task.setIsDelete(StateConstants.COMMON_ONE);
        ecmDestroyTaskMapper.updateById(task);
        return Result.success();
    }

    /**
     * 销毁清册
     */
    public Result destroyHistory(DestroyInfoVO destroyInfoVO) {
        AssertUtils.isNull(destroyInfoVO,"参数错误");
        PageHelper.startPage(destroyInfoVO.getPageNum(), destroyInfoVO.getPageSize());
        List<EcmDestroyTaskDTO> ecmDestroyListDTOS = ecmDestroyTaskMapper.selectDestroyList(destroyInfoVO.getDestroyTimeStart(), destroyInfoVO.getDestroyTimeEnd(),
                destroyInfoVO.getAppCodes(), destroyInfoVO.getDocCodes(), destroyInfoVO.getDestroyType(),
                destroyInfoVO.getBusiNo());
        //填充信息
        handleInfo(ecmDestroyListDTOS);
        return Result.success(new PageInfo<>(ecmDestroyListDTOS));
    }

    /**
     * 销毁
     */
    @Async("GlobalThreadPool")
    public void destroyEcm(){
        try {
            List<EcmDestroyTask> ecmDestroyTasks = ecmDestroyTaskMapper.selectList(
                    new LambdaQueryWrapper<EcmDestroyTask>()
                            .eq(EcmDestroyTask::getStatus, IcmsConstants.DESTROY_STATUS_ONE) // 待销毁任务
            );
            if (CollectionUtils.isEmpty(ecmDestroyTasks)) {
                return;
            }

            Set<Long> taskIds = ecmDestroyTasks.stream().map(EcmDestroyTask::getId).collect(Collectors.toSet());
            List<EcmDestroyList> allDestroyLists = ecmDestroyListMapper.selectList(
                    new LambdaQueryWrapper<EcmDestroyList>().in(EcmDestroyList::getDestroyTaskId, taskIds)
            );
            if (CollectionUtils.isEmpty(allDestroyLists)) {
                return;
            }
            // 预构建索引映射（O(1)查找）
            Map<Long, EcmDestroyTask> taskId2Task = ecmDestroyTasks.stream()
                    .collect(Collectors.toMap(
                            EcmDestroyTask::getId,
                            task -> task
                    ));
            Map<Long, EcmDestroyList> listId2List = allDestroyLists.stream()
                    .collect(Collectors.toMap(
                            EcmDestroyList::getId,
                            list -> list
                    ));
            Map<Long, List<EcmDestroyList>> taskId2DestroyLists = allDestroyLists.stream()
                    .collect(Collectors.groupingBy(EcmDestroyList::getDestroyTaskId));

            // 生成精准筛选规则
            List<DestroyFilterRuleVO> filterRules = new ArrayList<>();
            Set<Long> queryBusiIds = new HashSet<>();

            for (EcmDestroyTask task : ecmDestroyTasks) {
                Long taskId = task.getId();
                Integer destroyType = task.getDestroyType();
                List<EcmDestroyList> currentLists = taskId2DestroyLists.getOrDefault(taskId, new ArrayList<>());

                for (EcmDestroyList list : currentLists) {
                    DestroyFilterRuleVO rule = new DestroyFilterRuleVO();
                    rule.setTaskId(taskId);                  // 任务ID
                    rule.setDestroyListId(list.getId());    // 明细ID
                    rule.setBusiId(list.getBusiId());       // 业务ID
                    rule.setDocCode(list.getDocCode());     // 资料ID
                    rule.setDestroyType(destroyType);       // 销毁类型

                    if (IcmsConstants.DESTROY_TYPE_FOUR.equals(destroyType)) {
                        // 4:已删除节点彻底删除
                        rule.setIsDeleted(IcmsConstants.ONE);
                    } else {
                        // 0/1/2/3类型
                        rule.setIsDeleted(IcmsConstants.ZERO);
                    }
                    //  按销毁类型设置专属筛选字段（对齐你的查询条件）
                    if (IcmsConstants.DESTROY_TYPE_TWO.equals(destroyType)) {
                        // 已删除销毁
                        rule.setState(StateConstants.COMMON_ONE);
                    } else {
                        rule.setState(null);
                    }

                    filterRules.add(rule);
                    queryBusiIds.add(list.getBusiId());
                }
            }
            List<EcmFileInfo> allFileInfos = new ArrayList<>();
            if (!CollectionUtils.isEmpty(queryBusiIds)) {
                // 查询未删除文件（历史业务/资料销毁用）
                List<EcmFileInfo> normalFiles = ecmFileInfoMapper.selectList(
                        new LambdaQueryWrapper<EcmFileInfo>()
                                .in(EcmFileInfo::getBusiId, queryBusiIds)
                                .eq(EcmFileInfo::getIsDeleted, IcmsConstants.ZERO)
                );
                allFileInfos.addAll(normalFiles);
                // 查询已删除节点彻底删除（仅已删除节点彻底删除类型用）
                Set<Long> deleteBusiIds = filterRules.stream()
                        .filter(rule -> IcmsConstants.DESTROY_TYPE_FOUR.equals(rule.getDestroyType()))
                        .map(DestroyFilterRuleVO::getBusiId)
                        .collect(Collectors.toSet());
                if (!CollectionUtils.isEmpty(deleteBusiIds)) {
                    List<EcmFileInfo> deletedFiles = ecmFileInfoMapper.selectWithDeleteByBusiId(deleteBusiIds);
                    allFileInfos.addAll(deletedFiles);
                }
            }
            if (CollectionUtils.isEmpty(allFileInfos)) {
                return;
            }
            // 按规则过滤
            List<EcmFileInfo> waitDestroyFileInfos = new ArrayList<>();
            for (DestroyFilterRuleVO rule : filterRules) {
                List<EcmFileInfo> matchFiles = allFileInfos.stream()
                        .filter(file -> matchFileWithRule(file, rule))
                        .collect(Collectors.toList());
                waitDestroyFileInfos.addAll(matchFiles);
            }
            // 去重
            waitDestroyFileInfos = waitDestroyFileInfos.stream().distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(waitDestroyFileInfos)) {
                return;
            }
            //精准判断newFileId   文件是否被复用
            Set<Long> waitDestroyNewFileIds = waitDestroyFileInfos.stream()
                    .filter(f -> f.getNewFileId() != null)
                    .map(EcmFileInfo::getNewFileId)
                    .collect(Collectors.toSet());
            Map<Long, List<Long>> newFileId2WaitFileIds = waitDestroyFileInfos.stream()
                    .filter(f -> f.getNewFileId() != null)
                    .collect(Collectors.groupingBy(
                            EcmFileInfo::getNewFileId,
                            Collectors.mapping(EcmFileInfo::getFileId, Collectors.toList())
                    ));

            // 查询newFileId关联的所有文件（全量）
            List<EcmFileInfo> allRelateFiles = new ArrayList<>();
            if (!CollectionUtils.isEmpty(waitDestroyNewFileIds)) {
                allRelateFiles = ecmFileInfoMapper.selectList(
                        new LambdaQueryWrapper<EcmFileInfo>().in(EcmFileInfo::getNewFileId, waitDestroyNewFileIds)
                );
            }

            // 构建：newFileId → 关联的所有fileId
            Map<Long, Set<Long>> newFileId2AllFileIds = allRelateFiles.stream()
                    .collect(Collectors.groupingBy(
                            EcmFileInfo::getNewFileId,
                            Collectors.mapping(EcmFileInfo::getFileId, Collectors.toSet())
                    ));

            // 判断newFileId是否仅关联本次销毁的fileId（无外部复用）
            Set<Long> hasOuterReuseNewFileIds = new HashSet<>();
            for (Long newFileId : waitDestroyNewFileIds) {
                Set<Long> allRelateFileIds = newFileId2AllFileIds.getOrDefault(newFileId, new HashSet<>());
                List<Long> waitFileIds = newFileId2WaitFileIds.getOrDefault(newFileId, new ArrayList<>());

                if (!allRelateFileIds.isEmpty() && !waitFileIds.containsAll(allRelateFileIds)) {
                    hasOuterReuseNewFileIds.add(newFileId); // 标记有外部复用的newFileId
                }
            }

            for (DestroyFilterRuleVO rule : filterRules) {
                // 筛选当前规则下需要销毁的文件
                List<EcmFileInfo> needDestroyFiles = waitDestroyFileInfos.stream()
                        .filter(f -> Objects.equals(f.getBusiId(), rule.getBusiId())) // Long类型业务ID匹配
//                        .filter(f -> needDestroyNewFileIds.contains(f.getNewFileId()))
                        .filter(f -> matchFileWithRule(f, rule))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(needDestroyFiles)) {
                    continue;
                }

                // 提取销毁所需的fileId/newFileId
                List<Long> fileIds = needDestroyFiles.stream().map(EcmFileInfo::getFileId).collect(Collectors.toList());
                List<Long> newFileIds = needDestroyFiles.stream().map(EcmFileInfo::getNewFileId).collect(Collectors.toList());
                List<Long> finalNewFileIds = new ArrayList<>();
                if (!CollectionUtils.isEmpty(newFileIds)) {
                    for (Long newFileId : newFileIds) {
                        if (!hasOuterReuseNewFileIds.contains(newFileId)) {
                            finalNewFileIds.add(newFileId);
                        }
                    }
                }
                // 精准获取关联的任务/明细
                EcmDestroyTask targetTask = taskId2Task.get(rule.getTaskId());
                EcmDestroyList targetList = listId2List.get(rule.getDestroyListId());

                // 执行销毁
                try {
                    Map<String, String> appCodeAndNameMap = getAppCodeAndAppNameMap(Collections.singletonList(targetList));
                    delFileInfoAndExpandInfo(targetList, fileIds, finalNewFileIds, appCodeAndNameMap, targetTask);
                    fileInfoService.changeAfmData(targetList.getBusiId(), null, fileIds, IcmsConstants.TWO); // 对接你的IcmsConstants.TWO
                } catch (Exception e) {
                    log.error("销毁业务ID={}下的文件失败", targetList.getBusiId(), e);
                }
            }
        } catch (Exception e) {
            log.error("文件销毁任务执行异常", e);
        }
    }


    /**
     * 文件是否匹配筛选规则
     */
    private boolean matchFileWithRule(EcmFileInfo file, DestroyFilterRuleVO rule) {
        //基础校验：业务ID + 文件删除状态匹配
        if (!file.getBusiId().equals(rule.getBusiId()) // Long类型匹配
                || !file.getIsDeleted().equals(rule.getIsDeleted())) {
            return false;
        }

        //按销毁类型精准匹配
        Integer destroyType = rule.getDestroyType();
        switch (destroyType) {
            case DESTROY_TYPE_ZERO: // 0:历史业务销毁 → 仅业务ID匹配
            case DESTROY_TYPE_THREE: // 3:回收站业务 → 仅业务ID匹配
                return true;

            case DESTROY_TYPE_ONE: // 历史资料销毁 → 业务ID + 资料ID(docCode)匹配
                return !ObjectUtils.isEmpty(rule.getDocCode())
                        && rule.getDocCode().equals(file.getDocCode());

            case DESTROY_TYPE_TWO: //已删除销毁 → 业务ID + 状态(state)匹配
                return !ObjectUtils.isEmpty(rule.getState())
                        && rule.getState().equals(file.getState())
                        && StateConstants.COMMON_ONE.equals(file.getState()); // 强制匹配状态1

            case DESTROY_TYPE_FOUR: //已删除节点彻底删除 → 仅业务ID + 已删除状态匹配
                // 已删除销毁的核心是isDeleted=1（已在基础校验中完成），此处直接返回true
                return true;

            default:
                return false;
        }
    }

    private Map<String, String> getAppCodeAndAppNameMap(List<EcmDestroyList> ecmDestroyLists) {
        Set<String> appCodeSet = ecmDestroyLists.stream()
                .map(EcmDestroyList::getAppCode)
                .collect(Collectors.toSet());
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appCodeSet);
        return ecmAppDefs.stream()
                .collect(Collectors.toMap(EcmAppDef::getAppCode, EcmAppDef::getAppName));
    }

    /**
     * 销毁数据
     * @param ecmDestroyListDTOS
     * @param destroyInfoVO
     * @return
     */
    private List<EcmDestroyListDTO> handleDestroyLists(List<EcmDestroyListDTO> ecmDestroyListDTOS,DestroyInfoVO destroyInfoVO) {
        List<Long> busiIds = destroyInfoVO.getBusiIds();
        if(!CollectionUtils.isEmpty(busiIds)){
            ecmDestroyListDTOS = ecmDestroyListDTOS.stream().filter(s ->!busiIds.contains(s.getBusiId())).collect(Collectors.toList());
        }
        if (CollUtil.isEmpty(ecmDestroyListDTOS)) {
            return new ArrayList<>();
        }
        Set<String> appCodeSet = ecmDestroyListDTOS.stream()
                .map(EcmDestroyListDTO::getAppCode)
                .collect(Collectors.toSet());
        Set<String> docCodes = ecmDestroyListDTOS.stream()
                .map(EcmDestroyListDTO::getDocCode)
                .collect(Collectors.toSet());
        //业务类型
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appCodeSet);
        Map<String, String> appCodeAndNameMap = ecmAppDefs.stream()
                .collect(Collectors.toMap(EcmAppDef::getAppCode, EcmAppDef::getAppName));

        //处理数据

        if (IcmsConstants.DESTROY_TYPE_ZERO.equals(destroyInfoVO.getDestroyType())) {
            ecmDestroyListDTOS.forEach(s -> {
                if(s.getFileCount() == null){
                    Long l = ecmFileInfoMapper.selectCount(new LambdaQueryWrapper<EcmFileInfo>()
                        .eq(EcmFileInfo::getBusiId, s.getBusiId()));
                    s.setFileCount(l);
                }
                s.setAppTypeName("(" + s.getAppCode() + ")" + appCodeAndNameMap.get(s.getAppCode()));
            });
        } else if (IcmsConstants.DESTROY_TYPE_ONE.equals(destroyInfoVO.getDestroyType())) {
            List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectBatchIds(docCodes);
            Map<String, String> docCodeAndNameMap = ecmDocDefs.stream()
                    .collect(Collectors.toMap(EcmDocDef::getDocCode, EcmDocDef::getDocName));
            ecmDestroyListDTOS.forEach(s -> {
                if(s.getFileCount() == null){
                    Long l = ecmFileInfoMapper.selectCount(new LambdaQueryWrapper<EcmFileInfo>()
                        .eq(EcmFileInfo::getBusiId, s.getBusiId()).eq(EcmFileInfo::getDocCode, s.getDocCode()));
                    s.setFileCount(l);
                }
                s.setDocTypeName("(" + s.getDocCode() + ")" + docCodeAndNameMap.get(s.getDocCode()));
                s.setAppTypeName("(" + s.getAppCode() + ")" + appCodeAndNameMap.get(s.getAppCode()));
            });
        } else if (IcmsConstants.DESTROY_TYPE_TWO.equals(destroyInfoVO.getDestroyType())) {
            ecmDestroyListDTOS.forEach(s -> {
                if(s.getFileCount() == null){
                    Long l = ecmFileInfoMapper.selectCount(new LambdaQueryWrapper<EcmFileInfo>()
                        .eq(EcmFileInfo::getBusiId, s.getBusiId()).eq(EcmFileInfo::getState, StateConstants.COMMON_ONE));
                    s.setFileCount(l);
                }
                s.setAppTypeName("(" + s.getAppCode() + ")" + appCodeAndNameMap.get(s.getAppCode()));
            });
        }
        return ecmDestroyListDTOS;
    }

    private void checkVo(DestroyInfoVO destroyInfoVO) {
        AssertUtils.isNull(destroyInfoVO.getDestroyType(),"参数错误");
        AssertUtils.isNull(destroyInfoVO.getCreateTimeStart(),"参数错误");
        AssertUtils.isNull(destroyInfoVO.getCreateTimeEnd(),"参数错误");
        AssertUtils.isNull(destroyInfoVO.getOrgCode(),"参数错误");
        if(destroyInfoVO.getDestroyType().equals(IcmsConstants.DESTROY_TYPE_ZERO)){
            AssertUtils.isNull(destroyInfoVO.getAppCode(),"参数错误");
        }else if(destroyInfoVO.getDestroyType()==IcmsConstants.DESTROY_TYPE_ONE){
            AssertUtils.isNull(destroyInfoVO.getDocCode(),"参数错误");
        }
    }

    private LambdaQueryWrapper<EcmDestroyTask> getQueryWrapper(DestroyInfoVO destroyInfoVO) {
        LambdaQueryWrapper<EcmDestroyTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmDestroyTask::getIsDelete,StateConstants.ZERO);
        queryWrapper.ne(EcmDestroyTask::getDestroyType, IcmsConstants.DESTROY_TYPE_THREE);
        queryWrapper.ne(EcmDestroyTask::getDestroyType, IcmsConstants.DESTROY_TYPE_FOUR);
        // 判断 destroyType 是否为空
        if (!ObjectUtils.isEmpty(destroyInfoVO.getDestroyType())) {
            queryWrapper.eq(EcmDestroyTask::getDestroyType, destroyInfoVO.getDestroyType());
        }
        // 判断 appCode 是否为空
        if (!CollectionUtils.isEmpty(destroyInfoVO.getAppCodes())) {
            queryWrapper.in(EcmDestroyTask::getAppCode, destroyInfoVO.getAppCodes());
        }
        // 判断 docCode 是否为空
        if (!CollectionUtils.isEmpty(destroyInfoVO.getDocCodes())) {
            queryWrapper.in(EcmDestroyTask::getDocCode, destroyInfoVO.getDocCodes());
        }
        // 判断 orgCode 是否为空
        if (!ObjectUtils.isEmpty(destroyInfoVO.getOrgCode())) {
            queryWrapper.eq(EcmDestroyTask::getOrgCode, destroyInfoVO.getOrgCode());
        }
        queryWrapper.orderByDesc(EcmDestroyTask::getCreateTime);
        return queryWrapper;
    }

    /**
     * 业务及回收信息填充
     */
    private void handleInfo(List<EcmDestroyTaskDTO> ecmDestroyTaskDTOS) {
        if (CollUtil.isEmpty(ecmDestroyTaskDTOS)) {
            return;
        }
        Set<String> appCodeSet = ecmDestroyTaskDTOS.stream()
                .map(EcmDestroyTaskDTO::getAppCode)
                .collect(Collectors.toSet());
        Set<String> docCodes = ecmDestroyTaskDTOS.stream()
                .map(EcmDestroyTaskDTO::getDocCode)
                .collect(Collectors.toSet());
        List<EcmAppDef> ecmAppDefs = ecmAppDefMapper.selectBatchIds(appCodeSet);
        Map<String, String> appCodeAndNameMap = ecmAppDefs.stream()
                .collect(Collectors.toMap(EcmAppDef::getAppCode, EcmAppDef::getAppName));

        List<EcmDocDef> ecmDocDefs = ecmDocDefMapper.selectBatchIds(docCodes);
        Map<String, String> docCodeAndNameMap = ecmDocDefs.stream()
                .collect(Collectors.toMap(EcmDocDef::getDocCode, EcmDocDef::getDocName));
        for (EcmDestroyTaskDTO dto : ecmDestroyTaskDTOS) {
            switch (dto.getDestroyType()) {
                //魔法值
                case DESTROY_TYPE_ZERO:
                    dto.setDestroyTypeStr(IcmsConstants.DESTROY_TYPE_ZERO_STR);
                    dto.setBusiTypeStr("(" + dto.getAppCode() + ")" +appCodeAndNameMap.get(dto.getAppCode()));
                    break;
                case DESTROY_TYPE_ONE:
                    dto.setDestroyTypeStr(IcmsConstants.DESTROY_TYPE_ONE_STR);
                    dto.setBusiTypeStr("(" + dto.getDocCode() + ")" +docCodeAndNameMap.get(dto.getDocCode()));
                    break;
                case DESTROY_TYPE_TWO:
                    dto.setDestroyTypeStr(IcmsConstants.DESTROY_TYPE_TWO_STR);
                    break;
            }
            getStatusStr(dto);
        }
    }

    /**
     * 对相关表数据进行物理删除
     * @param destroyList
     * @param fileIds
     * @param newFileIds
     * @param appCodeAndNameMap
     * @param task
     */
    @Transactional(rollbackFor = Exception.class)
    public void delFileInfoAndExpandInfo(EcmDestroyList destroyList,List<Long> fileIds,List<Long> newFileIds,
                        Map<String, String> appCodeAndNameMap,EcmDestroyTask task){
        if(CollectionUtils.isEmpty(fileIds)){
            task.setStatus(IcmsConstants.DESTROY_STATUS_THREE);
            task.setDestroyTime(new Date());
            ecmDestroyTaskMapper.updateById(task);
            return;
        }
        if(!CollectionUtils.isEmpty(newFileIds)){
            //调用存储接口删除st_file表信息以及文件
            Result result = fileStorageApi.delBatch(newFileIds);
            if (!result.isSucc()){
                log.error("销毁定时任务调用底座删除接口失败,newFileId:"+newFileIds.toString());
                return;
            }
        }
        //ecm_file_info
        ecmFileInfoMapper.deleteBatchByFileId(fileIds);
        //ecm_file_history
        ecmFileHistoryMapper.deleteBatchByFileId(fileIds);
        //ecm_file_label
        ecmFileLabelMapper.deleteBatchByFileId(fileIds);
        //ecm_file_mark_comment
        ecmFileMarkCommentMapper.deleteBatchByFileId(fileIds);
        //ecm_file_comment
        ecmFileCommentMapper.deleteBatchByFileId(fileIds);
        //ecm_file_batch_operation
        ecmFileBatchOperationMapper.deleteBatchByFileId(fileIds);
        //ecm_async_task
        ecmAsyncTaskMapper.deleteBatchByFileId(fileIds);
        //删除es数据
        esEcmFileMapper.deleteBatchIds(fileIds, fileIndex);
        //清除redis文件数据信息
        busiCacheService.delFileInfoRedisReal(destroyList.getBusiId());
        // 如果业务内所有文件已被销毁则删除业务
        Long fileids = ecmFileInfoMapper.selectCount(new LambdaQueryWrapper<EcmFileInfo>()
                .eq(EcmFileInfo::getBusiId, destroyList.getBusiId()));
        if(fileids == 0){
            ecmBusiInfoMapper.deleteByBusiId(destroyList.getBusiId());
            //删除redis业务信息
            busiCacheService.delBusiInfo(destroyList.getBusiId());
            // 批量删除es数据
            esEcmBusiMapper.deleteById(destroyList.getBusiId(), busiIndex);
        }

        //记录业务日志
        //记录日志
        EcmBusiLog ecmBusiLog = new EcmBusiLog();
        ecmBusiLog.setBusiNo(destroyList.getBusiNo());
        ecmBusiLog.setAppName(appCodeAndNameMap.get(destroyList.getAppCode()));
        ecmBusiLog.setAppCode(destroyList.getAppCode());
        ecmBusiLog.setOrgCode(destroyList.getOrgCode());
        ecmBusiLog.setOperatorId(task.getCreateUser());
        ecmBusiLog.setOperator(task.getCreateUserName());
        ecmBusiLog.setOperatorType(BusiLogConstants.OPERATION_TYPE_EIGHT);
        ecmBusiLog.setOperateContent("销毁文件" + ":" + fileIds.get(0)+"等"+fileIds.size() + "个文件");
        ecmBusiLogMapper.insert(ecmBusiLog);
        task.setStatus(IcmsConstants.DESTROY_STATUS_THREE);
        task.setDestroyTime(new Date());
        ecmDestroyTaskMapper.updateById(task);
    }
}
