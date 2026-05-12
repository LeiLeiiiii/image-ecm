package com.sunyard.ecm.task;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.ecm.config.MqConfig;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.manager.AsyncTaskService;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.service.mq.RabbitMQProducer;
import com.sunyard.framework.quartz.annotation.QuartzLog;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 @Author yzy
 @desc 每小时查询队列失效的信息进行补偿
 @time 2025/06/23 13:37
 **/
@Slf4j
@DisallowConcurrentExecution
public class EcmBusiAsyncPushMQTask extends QuartzJobBean {
    @Resource
    private AsyncTaskService asyncTaskService;
    @Resource
    private RabbitMQProducer rabbitMQProducer;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private MqConfig mqConfig;

    @Override
    @QuartzLog
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("####### ecmBusiAsyncPushMQTask 智能化任务MQ补偿开始 ##########");
        List<EcmAsyncTask> tasks = asyncTaskService.getEcmAsyncTaskListInMq();
        log.info("####### 补偿数量为{} ##########", tasks.size());

        List<EcmAsyncTask> tasksToUpdate = new ArrayList<>();
        Date now = new Date();

        tasks.forEach(task -> {
            try {
                String taskType = task.getTaskType();
                EcmFileInfoDTO fileInfo = busiCacheService.getFileInfoRedisSingle(task.getBusiId(), task.getFileId());

                // 计算距上次补偿/创建的时间差
                Date referenceTime = Optional.ofNullable(task.getLastCompensateTime())
                        .orElse(task.getCreateTime());
                long diffHours = Duration.between(referenceTime.toInstant(), now.toInstant()).toHours();

                boolean shouldCompensate = false;

                // 检查任务类型并决定是否补偿
                for (Map.Entry<Integer, String> entry : getRoutingKeyMap().entrySet()) {
                    int position = entry.getKey();
                    if (taskType.charAt(position - 1) == EcmCheckAsyncTaskEnum.IN_MQ.description().charAt(0)) {
                        if (position == IcmsConstants.TYPE_FOUR && diffHours >= 10) {
                            // 查重任务
                            sendCompensationMessage(task, fileInfo, entry.getValue(), TimeUnit.HOURS.toMillis(10));
                            shouldCompensate = true;
                        } else if (diffHours >= 1) {
                            // 其他任务
                            sendCompensationMessage(task, fileInfo, entry.getValue(), TimeUnit.HOURS.toMillis(1));
                            shouldCompensate = true;
                        }
                    }
                }

                if (shouldCompensate) {
                    task.setLastCompensateTime(now);
                    tasksToUpdate.add(task);
                }
            } catch (Exception e) {
                log.error("补偿任务处理失败: fileId={}", task.getFileId(), e);
            }
        });

        // 批量更新需要补偿的任务
        if (!tasksToUpdate.isEmpty()) {
            asyncTaskService.batchUpdateTask(tasksToUpdate);
        }
    }

    private void sendCompensationMessage(EcmAsyncTask task, EcmFileInfoDTO fileInfo,
                                         String routingKey, long ttlMs) {
        try {
            rabbitMQProducer.sendMessageWithTTL(
                    JSONObject.toJSONString(fileInfo),
                    mqConfig.getExchangeEcmIntelligent(),
                    routingKey,
                    ttlMs
            );
        } catch (Exception e) {
            log.error("MQ消息发送失败: fileId={}, routingKey={}", task.getFileId(), routingKey, e);
            throw new RuntimeException("MQ发送失败", e);
        }
    }

    private Map<Integer, String> getRoutingKeyMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(IcmsConstants.TYPE_ONE, "docOcr");
        map.put(IcmsConstants.TYPE_TWO, "regularize");
        map.put(IcmsConstants.TYPE_THREE, "obscure");
        map.put(IcmsConstants.TYPE_FOUR, "afm");
        map.put(IcmsConstants.TYPE_SIX, "remake");
        map.put(IcmsConstants.TYPE_SEVEN, "esContext");
        map.put(IcmsConstants.TYPE_EIGHT, "reflective");
        map.put(IcmsConstants.TYPE_NINE, "missCorner");
        return map;
    }
}

