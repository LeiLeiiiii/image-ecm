package com.sunyard.ecm.task;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.ecm.config.MqConfig;
import com.sunyard.ecm.config.properties.EcmRetryTaskProperties;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.manager.AsyncTaskService;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.ecm.service.mq.RabbitMQProducer;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@DisallowConcurrentExecution
public class EcmIntelligentRetryTask  extends QuartzJobBean {

    @Resource
    private AsyncTaskService asyncTaskService;
    @Resource
    private RabbitMQProducer rabbitMQProducer;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private MqConfig mqConfig;
    @Resource
    private EcmRetryTaskProperties ecmRetryTaskProperties;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("####### EcmIntelligentRetryTask 智能化任务重试开始 ##########");
        List<EcmAsyncTask> tasks = asyncTaskService.getEcmAsyncTaskListInRetry(ecmRetryTaskProperties);
        log.info("####### 重试数量为：{} ##########", tasks.size());

        tasks.forEach(task -> {
            try {
                String taskType = task.getTaskType();
                //将全部失败的状态改成IN_MQ
                taskType = taskType.replace(EcmCheckAsyncTaskEnum.FAILED.description(),
                        EcmCheckAsyncTaskEnum.IN_MQ.description());
                EcmFileInfoDTO fileInfo = busiCacheService.getFileInfoRedisSingle(task.getBusiId(), task.getFileId());

                // 检查任务类型并决定是否补偿
                for (Map.Entry<Integer, String> entry : getRoutingKeyMap().entrySet()) {
                    int position = entry.getKey();
                    if (taskType.charAt(position - 1) == EcmCheckAsyncTaskEnum.IN_MQ.description().charAt(0)) {

                        if (position == IcmsConstants.TYPE_TEN){
                            taskType=updateStatus(taskType, Arrays.asList(IcmsConstants.TYPE_TEN,IcmsConstants.TYPE_SEVEN),EcmCheckAsyncTaskEnum.IN_MQ.description().charAt(0));
                            task.setTaskType(taskType);
                        }
                        task.setTaskType(taskType);
                        asyncTaskService.updateEcmAsyncTask(task);
                        sendCompensationMessage(task, fileInfo, entry.getValue(), TimeUnit.HOURS.toMillis(1));
                    }
                }
                //文件es状态
                if (taskType.charAt(IcmsConstants.TYPE_ELEVEN - 1)== EcmCheckAsyncTaskEnum.IN_MQ.description().charAt(0)){
                    //发送es补偿消息
                    operateFullQueryService.addEsFileInfo(fileInfo, null);
                    log.info("文件es补偿信息推送完成！");
                }
                //修改重试状态 设置为重试中:0
                task.setIsFail(IcmsConstants.ZERO);
                //重试次数+1
                task.setRetryCount(task.getRetryCount()+1);
            } catch (Exception e) {
                log.error("重试任务处理失败: fileId={}", task.getFileId(), e);
            }
        });

        //更新重试次数
        if (!tasks.isEmpty()) {
            asyncTaskService.batchUpdateTask(tasks);
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
        map.put(IcmsConstants.TYPE_ONE, "docOcrAndTextDup");
        map.put(IcmsConstants.TYPE_TWO, "regularize");
        map.put(IcmsConstants.TYPE_THREE, "obscure");
        map.put(IcmsConstants.TYPE_FOUR, "imageDup");
        map.put(IcmsConstants.TYPE_SIX, "remake");
        map.put(IcmsConstants.TYPE_SEVEN, "esContext");
        map.put(IcmsConstants.TYPE_EIGHT, "reflective");
        map.put(IcmsConstants.TYPE_NINE, "missCorner");
        map.put(IcmsConstants.TYPE_TEN,"esContext");
        return map;
    }

    private String updateStatus(String status, List<Integer> positions, char newValue) {
        StringBuilder sb = new StringBuilder(status);
        positions.forEach(position->{
            if (position < 1 || position > IcmsConstants.LENGTH) {
                throw new IllegalArgumentException(
                        String.format("只能更新 1 到 %d 位", IcmsConstants.LENGTH)
                );
            }
            sb.setCharAt(position - 1, newValue);
        });
        return sb.toString();
    }
}
