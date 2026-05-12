package com.sunyard.ecm.manager;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.ecm.config.MqConfig;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.enums.EcmCheckAsyncTaskEnum;
import com.sunyard.ecm.po.EcmAsyncTask;
import com.sunyard.ecm.service.mq.RabbitMQProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yzy
 * @desc 智能化检测处理
 * @since 2026/2/3
 */
@Slf4j
@Service
public class CheckDetectionService {

    @Resource
    private MqConfig mqConfig;
    @Resource
    private RabbitMQProducer rabbitMQProducer;
    @Resource
    private AsyncTaskService asyncTaskService;

    /**
     * 上传文件检测处理-MQ
     */
    public void checkDetectionByMq(EcmFileInfoDTO ecmFileInfoDTO, EcmAsyncTask ecmAsyncTask, String taskType) {
        String exchange = mqConfig.getExchangeEcmIntelligent();
        // 定义类型和路由键的映射
        Map<Integer, String> typeRoutingKeyMap = new HashMap<>();
        typeRoutingKeyMap.put(IcmsConstants.TYPE_ONE, "docOcrAndTextDup");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_TWO, "regularize");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_THREE, "obscure");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_FOUR, "imageDup");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_SIX, "remake");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_SEVEN, "esContext");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_EIGHT, "reflective");
        typeRoutingKeyMap.put(IcmsConstants.TYPE_NINE, "missCorner");


        char targetChar = EcmCheckAsyncTaskEnum.PROCESSING.description().charAt(0);
        // 检查转正和缺角是否同时开启
        boolean isTypeTwoActive = taskType.charAt(IcmsConstants.TYPE_TWO - 1) == targetChar;
        boolean isTypeNineActive = taskType.charAt(IcmsConstants.TYPE_NINE - 1) == targetChar;
        boolean useSpecialRouting = isTypeTwoActive && isTypeNineActive;

        // 遍历映射，检查并发送消息
        boolean isSpecialSent = false; // 标记是否已用special路由键发送过

        for (Map.Entry<Integer, String> entry : typeRoutingKeyMap.entrySet()) {
            int position = entry.getKey();
            String routingKey = entry.getValue();

            if (taskType.charAt(position - 1) == targetChar) {
                // 当未发送过special且满足2或9的条件时，使用special路由键
                String actualRoutingKey;
                if (!isSpecialSent && useSpecialRouting &&
                        (position == IcmsConstants.TYPE_TWO || position == IcmsConstants.TYPE_NINE)) {
                    actualRoutingKey = "special";
                    isSpecialSent = true; // 标记已发送，避免重复
                } else {
                    actualRoutingKey = routingKey;
                }

                // 发送消息（根据position设置TTL）
                if (position == IcmsConstants.TYPE_FOUR) {
                    rabbitMQProducer.sendMessageWithTTL(
                            JSONObject.toJSONString(ecmFileInfoDTO),
                            exchange,
                            actualRoutingKey,
                            IcmsConstants.AFM_MQ_DISTIME
                    );
                } else {
                    rabbitMQProducer.sendMessageWithTTL(
                            JSONObject.toJSONString(ecmFileInfoDTO),
                            exchange,
                            actualRoutingKey,
                            IcmsConstants.INTELLIGENT_DETECTION_DISTIME
                    );
                }
            }
        }
        // 修改异步任务状态为已存入队列
        taskType = taskType.replace(EcmCheckAsyncTaskEnum.PROCESSING.description(),
                EcmCheckAsyncTaskEnum.IN_MQ.description());
        ecmAsyncTask.setTaskType(taskType);
        asyncTaskService.updateEcmAsyncTask(ecmAsyncTask);
    }
}
