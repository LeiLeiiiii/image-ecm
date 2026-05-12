package com.sunyard.module.storage.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyard.module.storage.config.factory.FileStorage;
import com.sunyard.module.storage.config.factory.impl.FileStorageService;
import com.sunyard.module.storage.constant.RedisTopicConstant;
import com.sunyard.module.storage.dto.FileStorageEquipmentTopicDTO;
import com.sunyard.module.storage.manager.StorageEquipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yzy
 * @desc 文件存储设备配置同步监听器（消费端）
 * @since 2025/12/18
 */
@Slf4j
@Component
public class FileStorageEquipmentListener {

    // JSON序列化工具
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private RedisMessageListenerContainer redisMessageListenerContainer;
    @Resource
    private StorageEquipmentService storageEquipmentService;

    @PostConstruct
    public void initListener() {
        // 绑定Redis监听器
        redisMessageListenerContainer.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                try {
                    // 反序列化消息
                    String msgBody = new String(message.getBody());
                    FileStorageEquipmentTopicDTO dto = objectMapper.readValue(msgBody, FileStorageEquipmentTopicDTO.class);
                    // 构建新的FileStorage列表
                    CopyOnWriteArrayList<FileStorage> oldFileStorageList = fileStorageService.getFileStorageList();
                    CopyOnWriteArrayList<FileStorage> newFileStorageList = storageEquipmentService.getFileStorageList(
                            dto.getStorageType(), dto.getStEquipmentList(), oldFileStorageList);
                    // 空列表校验
                    if (newFileStorageList == null || newFileStorageList.isEmpty()) {
                        log.warn("Redis广播消息为空，忽略处理");
                        return;
                    }

                    // 直接同步新列表到本地
                    fileStorageService.setFileStorageList(newFileStorageList);
                    log.info("消费端同步FileStorage列表成功，列表大小：{}", newFileStorageList.size());

                } catch (Exception e) {
                    log.error("消费Redis广播消息失败", e);
                }
            }
        }, new ChannelTopic(RedisTopicConstant.FILE_STORAGE_EQUIPMENT_TOPIC));
    }
}