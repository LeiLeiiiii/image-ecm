package com.sunyard.ecm.manager;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.ecm.annotation.WebsocketNoticeAnnotation;
import com.sunyard.ecm.config.properties.EcmRetryTaskProperties;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.dto.ecm.EcmAsyncTaskDTO;
import com.sunyard.ecm.dto.ecm.EcmAsyncTaskGroupDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.mapper.EcmAsyncTaskMapper;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.vo.EcmAsyncTaskVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.framework.redis.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yzy
 * @desc 异步任务栏列表接口
 * @since 2025/3/3
 */
@Slf4j
@Service
public class AsyncTaskService {

    @Resource
    private EcmAsyncTaskMapper asyncTaskMapper;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 根据业务获取异步任务列表
     */
    public Result<EcmAsyncTaskVO> getEcmAsyncTaskList(Long busiId) {
        AssertUtils.isNull(busiId, "业务ID不能为空");
        List<EcmAsyncTask> ecmAsyncTasks = busiCacheService.getEcmAsyncTaskList(busiId);
        List<FileInfoRedisDTO> fileInfos = busiCacheService.getFileInfoRedis(busiId);

        // 创建一个Map，便于根据文件ID快速查找文件名
        Map<String, String> fileIdToNameMap = new HashMap<>();
        Map<String, String> fileIdTonewFileIdMap = new HashMap<>();
        for (FileInfoRedisDTO fileInfo : fileInfos) {
            fileIdToNameMap.put(String.valueOf(fileInfo.getFileId()), fileInfo.getNewFileName());
            fileIdTonewFileIdMap.put(String.valueOf(fileInfo.getFileId()), String.valueOf(fileInfo.getNewFileId()));
        }
        Map<String, List<String>> newFileIdToFileIdMap = buildReverseMap(fileIdTonewFileIdMap);

        // 创建任务ID和名称的Map，将模糊检测、反光检测、缺角检测合并为质量检测
        Map<Integer, String> taskIdToNameMap = new HashMap<>();
        taskIdToNameMap.put(IcmsConstants.TYPE_ONE, "文档识别");
        taskIdToNameMap.put(IcmsConstants.TYPE_TWO, "自动转正");
        taskIdToNameMap.put(IcmsConstants.TYPE_THREE, "质量检测");
        taskIdToNameMap.put(IcmsConstants.TYPE_FOUR, "图像查重");
        taskIdToNameMap.put(IcmsConstants.TYPE_FIVE, "拆分合并");
        taskIdToNameMap.put(IcmsConstants.TYPE_SIX, "翻拍检测");
        taskIdToNameMap.put(IcmsConstants.TYPE_TEN, "文本查重");

        // 存储属于质量检测的任务ID及其对应的索引
        Map<Integer, Integer> qualityCheckTaskIndices = new HashMap<>();
        // 模糊检测，对应状态第一位
        qualityCheckTaskIndices.put(IcmsConstants.TYPE_THREE, 0);
        // 反光检测，对应状态第二位
        qualityCheckTaskIndices.put(IcmsConstants.TYPE_EIGHT, 1);
        // 缺角检测，对应状态第三位
        qualityCheckTaskIndices.put(IcmsConstants.TYPE_NINE, 2);

        // 使用 Map 存储每个任务的统计信息
        Map<String, EcmAsyncTaskGroupDTO> taskMap = new HashMap<>();
        for (Map.Entry<Integer, String> entry : taskIdToNameMap.entrySet()) {
            Integer taskId = entry.getKey();
            String taskName = entry.getValue();
            EcmAsyncTaskGroupDTO taskDTO = new EcmAsyncTaskGroupDTO();
            taskDTO.setTaskName(taskName);
            taskDTO.setTaskId(taskId);
            taskDTO.setTotal(0);
            taskDTO.setProcessingCount(0);
            taskDTO.setData(new ArrayList<>());
            taskMap.put(taskName, taskDTO);
        }

        // 分组统计
        for (EcmAsyncTask ecmAsyncTask : ecmAsyncTasks) {
            String taskType = ecmAsyncTask.getTaskType();

            // 跟踪已处理的文件ID，避免重复统计
            Set<Long> processedFileIds = new HashSet<>();

            for (Integer taskId : taskIdToNameMap.keySet()) {
                int taskIndex = taskId - 1;
                String taskName = taskIdToNameMap.get(taskId);

                // 处理质量检测特殊情况，按文件维度合并统计
                if (IcmsConstants.TYPE_THREE.equals(taskId)) {
                    Long fileId = ecmAsyncTask.getFileId();
                    // 确保每个文件只统计一次
                    if (!processedFileIds.contains(fileId)) {
                        processedFileIds.add(fileId);

                        // 构建3位状态字符串：模糊检测+反光检测+缺角检测
                        StringBuilder qualityStatus = new StringBuilder();
                        boolean hasNonInitialStatus = false;

                        // 收集质量检测相关的所有状态
                        for (Map.Entry<Integer, Integer> entry : qualityCheckTaskIndices.entrySet()) {
                            int type = entry.getKey();
                            int statusIndex = type - 1;

                            if (statusIndex < taskType.length()) {
                                char status = taskType.charAt(statusIndex);
                                qualityStatus.append(status);
                                if (status != EcmCheckAsyncTaskEnum.INITIAL_STATE.description().charAt(0)) {
                                    hasNonInitialStatus = true;
                                }
                            } else {
                                qualityStatus.append(EcmCheckAsyncTaskEnum.INITIAL_STATE.description().charAt(0));
                            }
                        }

                        // 只有当存在非初始状态时才统计
                        if (hasNonInitialStatus) {
                            EcmAsyncTaskGroupDTO taskDTO = taskMap.get(taskName);
                            taskDTO.setTotal(taskDTO.getTotal() + 1);

                            // 检查是否有处理中的状态
                            char[] statusChars = qualityStatus.toString().toCharArray();
                            for (char c : statusChars) {
                                if (c == EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0) ||
                                        c == EcmCheckAsyncTaskEnum.IN_MQ.description().charAt(0)) {
                                    taskDTO.setProcessingCount(taskDTO.getProcessingCount() + 1);
                                    break; // 只要有一个处理中，就计数一次
                                }
                            }

                            // 创建并配置EcmAsyncTaskDTO副本
                            EcmAsyncTaskDTO ecmAsyncTaskCopy = createTaskDTOCopy(ecmAsyncTask,
                                    qualityStatus.toString(),
                                    fileId, taskId,
                                    fileIdToNameMap,
                                    fileIdTonewFileIdMap,
                                    newFileIdToFileIdMap);

                            // 将副本添加到任务数据列表
                            taskDTO.getData().add(ecmAsyncTaskCopy);
                        }
                    }
                } else {
                    // 处理其他任务类型
                    if (taskIndex < taskType.length()) {
                        char status = taskType.charAt(taskIndex);
                        if (status != EcmCheckAsyncTaskEnum.INITIAL_STATE.description().charAt(0)) {
                            EcmAsyncTaskGroupDTO taskDTO = taskMap.get(taskName);
                            taskDTO.setTotal(taskDTO.getTotal() + 1);

                            if (status == EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0) ||
                                    status == EcmCheckAsyncTaskEnum.IN_MQ.description().charAt(0)) {
                                taskDTO.setProcessingCount(taskDTO.getProcessingCount() + 1);
                            }

                            // 创建并配置EcmAsyncTaskDTO副本
                            EcmAsyncTaskDTO ecmAsyncTaskCopy = createTaskDTOCopy(ecmAsyncTask,
                                    String.valueOf(status),
                                    ecmAsyncTask.getFileId(),
                                    taskId,
                                    fileIdToNameMap,
                                    fileIdTonewFileIdMap,
                                    newFileIdToFileIdMap);

                            // 将副本添加到任务数据列表
                            taskDTO.getData().add(ecmAsyncTaskCopy);
                        }
                    }
                }
            }
        }

        // 在每个任务组的 data 列表中按 update_time 排序，处理 null 值
        for (EcmAsyncTaskGroupDTO taskDTO : taskMap.values()) {
            taskDTO.getData().sort(Comparator.comparing(EcmAsyncTaskDTO::getUpdateTime,
                    Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
        }

        // 构造结果对象
        EcmAsyncTaskVO result = new EcmAsyncTaskVO();
        List<EcmAsyncTaskGroupDTO> ecmAsyncTaskGroupDTOList = new ArrayList<>(taskMap.values());
        result.setEcmAsyncTaskGroupDTOList(ecmAsyncTaskGroupDTOList);

        return Result.success(result);
    }

    /**
     * 创建EcmAsyncTaskDTO副本并设置相关属性
     */
    private EcmAsyncTaskDTO createTaskDTOCopy(EcmAsyncTask original, String status, Long fileId, Integer taskId,
                                              Map<String, String> fileIdToNameMap,
                                              Map<String, String> fileIdTonewFileIdMap,
                                              Map<String, List<String>> newFileIdToFileIdMap) {
        EcmAsyncTaskDTO copy = new EcmAsyncTaskDTO();
        BeanUtil.copyProperties(original, copy);
        copy.setStatus(status);

        // 获取文件名和newFileId
        Map<String, String> fileInfo = getFileNameAndNewFileId(fileId, taskId, fileIdToNameMap,
                fileIdTonewFileIdMap, newFileIdToFileIdMap);

        if (fileInfo.get("fileName") != null) {
            copy.setFileName(fileInfo.get("fileName"));
        }
        if (fileInfo.get("newFileId") != null) {
            copy.setNewFileId(Long.valueOf(fileInfo.get("newFileId")));
        }

        return copy;
    }

    /**
     * 获取文件名和newFileId，使用Map返回结果
     */
    private Map<String, String> getFileNameAndNewFileId(Long fileId, Integer taskId,
                                                        Map<String, String> fileIdToNameMap,
                                                        Map<String, String> fileIdTonewFileIdMap,
                                                        Map<String, List<String>> newFileIdToFileIdMap) {
        Map<String, String> result = new HashMap<>(2);
        String newFileId = fileIdTonewFileIdMap.get(fileId.toString());
        String fileName = fileIdToNameMap.get(fileId.toString());

        // 如果是合并,异步任务中的fileId实际是文件的newFileId
        if (IcmsConstants.TYPE_FIVE.equals(taskId) && fileName == null) {
            newFileId = String.valueOf(fileId);
            List<String> list = newFileIdToFileIdMap.get(newFileId);
            if (CollectionUtils.isNotEmpty(list)) {
                fileId = Long.valueOf(list.get(0));
                fileName = fileIdToNameMap.get(fileId.toString());
            }
        }

        result.put("fileName", fileName);
        result.put("newFileId", newFileId);
        return result;
    }


    /**
     * 根据文件ID修改任务状态
     */
    public void updateEcmAsyncTask(EcmAsyncTask ecmAsyncTask) {
        LambdaQueryWrapper<EcmAsyncTask> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(EcmAsyncTask::getBusiId,ecmAsyncTask.getBusiId());
        lambdaQueryWrapper.eq(EcmAsyncTask::getFileId,ecmAsyncTask.getFileId());
        ecmAsyncTask.setUpdateTime(new Date());
        asyncTaskMapper.update(ecmAsyncTask,lambdaQueryWrapper);
        //写入redis缓存
        updateBusiAsyncTaskRedis(ecmAsyncTask);
    }

    /**
     * 根据文件ID修改任务状态
     */
    public void updateEcmAsyncTaskRedis(EcmAsyncTask ecmAsyncTask) {
        //写入redis缓存
        updateBusiAsyncTaskRedis(ecmAsyncTask);
    }

    /**
     * 根据文件ID,业务ID获取任务
     */
    public EcmAsyncTask getEcmAsyncTask(Long busiId, Long fileId) {
        LambdaQueryWrapper<EcmAsyncTask> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(EcmAsyncTask::getBusiId,busiId);
        lambdaQueryWrapper.eq(EcmAsyncTask::getFileId,fileId);
        return asyncTaskMapper.selectOne(lambdaQueryWrapper);
    }

    /**
     * 插入
     */
    public void insert(EcmAsyncTask ecmAsyncTask) {
         asyncTaskMapper.insert(ecmAsyncTask);
         //写入redis缓存
        updateBusiAsyncTaskRedis(ecmAsyncTask);
    }

    /**
     * 批量初始化任务
     */
    public void batchInsert(List<EcmAsyncTask> ecmAsyncTaskList) {
        MybatisBatch<EcmAsyncTask> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmAsyncTaskList);
        MybatisBatch.Method<EcmAsyncTask> method = new MybatisBatch.Method<>(EcmAsyncTaskMapper.class);
        mybatisBatch.execute(method.insert());
        //写入redis缓存
        updateBusiAsyncTaskRedis(ecmAsyncTaskList);
    }

    /**
     * 推送异步任务栏
     */
    @WebsocketNoticeAnnotation(busiId = "#busiIds",msg = "asyncTask")
    public void pushAsyncTaskList(List<String> busiIds) {
        log.info("推送异步任务栏业务列表:{}",busiIds);
        //遍历业务,检测完毕的需要清掉redis
        for(String busiId:busiIds){
            String key=RedisConstants.NEED_PUSH_BUSIASYNC_TASK_PREFIX+busiId;
            String value=redisUtils.get(key);
            if(IcmsConstants.DETECTION_COMPLETE.equals(value)){
                redisUtils.del(key);
            }
        }
    }

    /**
     * 批量删除
     */
    public void batchDelete(List<Long> ids,Long busiId) {
        asyncTaskMapper.deleteBatchIds(ids);
        //删除缓存
        deleteBusiAsyncTaskRedis(ids,busiId);
    }


    /**
     * 批量更新
     */
    public void batchUpdateTask(List<EcmAsyncTask> ecmAsyncTaskList) {
        MybatisBatch<EcmAsyncTask> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmAsyncTaskList);
        MybatisBatch.Method<EcmAsyncTask> method = new MybatisBatch.Method<>(EcmAsyncTaskMapper.class);
        mybatisBatch.execute(method.updateById());
        //写入redis缓存
        updateBusiAsyncTaskRedis(ecmAsyncTaskList);
    }

    /**
     * 获取目前在处理中的异步任务
     */
    public List<EcmAsyncTask> getEcmAsyncTaskListInProcessing() {
        LambdaQueryWrapper<EcmAsyncTask> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(EcmAsyncTask::getTaskType,"1");
        return asyncTaskMapper.selectList(lambdaQueryWrapper);
    }

    /**
     * 获取目前在队列中的异步任务
     */
    public List<EcmAsyncTask> getEcmAsyncTaskListInMq() {
        LambdaQueryWrapper<EcmAsyncTask> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(EcmAsyncTask::getIsCompensate,IcmsConstants.ONE);
        return asyncTaskMapper.selectList(lambdaQueryWrapper);
    }

    /**
     * 获取失败需要重试的任务
     */
    public List<EcmAsyncTask> getEcmAsyncTaskListInRetry(EcmRetryTaskProperties ecmRetryTaskProperties) {
        LambdaQueryWrapper<EcmAsyncTask> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(EcmAsyncTask::getIsFail,IcmsConstants.TWO);
        lambdaQueryWrapper.le(EcmAsyncTask::getRetryCount,ecmRetryTaskProperties.getMaxRetryCount());
//        lambdaQueryWrapper.last("LIMIT "+ecmRetryTaskProperties.getLimitCount());
        int limitCount = Integer.valueOf(ecmRetryTaskProperties.getLimitCount());
        Page<EcmAsyncTask> page = new Page<>(1, limitCount);
        IPage<EcmAsyncTask> resultPage = asyncTaskMapper.selectPage(page, lambdaQueryWrapper);
//        return asyncTaskMapper.selectList(lambdaQueryWrapper);
        return resultPage.getRecords();
    }

    /**
     * 将异步任务写入redis缓存
     */
    public void updateBusiAsyncTaskRedis(EcmAsyncTask ecmAsyncTask){
        redisUtils.hset(RedisConstants.BUSIASYNC_TASK_PREFIX + ecmAsyncTask.getBusiId(),
                ecmAsyncTask.getFileId().toString(),
                ecmAsyncTask, TimeOutConstants.SEVEN_DAY);
    }

    /**
     * 将异步任务列表写入redis缓存
     */
    public void updateBusiAsyncTaskRedis(List<EcmAsyncTask> ecmAsyncTasks){
        for(EcmAsyncTask ecmAsyncTask:ecmAsyncTasks){
            redisUtils.hset(RedisConstants.BUSIASYNC_TASK_PREFIX + ecmAsyncTask.getBusiId(),
                    ecmAsyncTask.getFileId().toString(),
                    ecmAsyncTask, TimeOutConstants.SEVEN_DAY);
        }
    }

    /**
     * 删除缓存
     */
    public void deleteBusiAsyncTaskRedis(List<Long> ids,Long busiId){
        for(Long fileId:ids){
            redisUtils.hdel(RedisConstants.BUSIASYNC_TASK_PREFIX + busiId,
                    fileId.toString());
        }
    }

    /**
     * 构建value到key的反向映射
     */
    public static <K, V> Map<V, List<K>> buildReverseMap(Map<K, V> originalMap) {
        Map<V, List<K>> reverseMap = new HashMap<>();

        for (Map.Entry<K, V> entry : originalMap.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();

            // 如果value不存在，创建新的List
            reverseMap.computeIfAbsent(value, k -> new ArrayList<>()).add(key);
        }

        return reverseMap;
    }


}
