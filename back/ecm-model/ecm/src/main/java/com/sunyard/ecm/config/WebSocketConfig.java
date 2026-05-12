package com.sunyard.ecm.config;
/*
 * Project: sunicms
 *
 * File Created at 2024/6/17
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.ecm.websocket.RedisListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author Leo
 * @Desc
 * @date 2024/6/17 13:58
 */
@Slf4j
@Configuration
public class WebSocketConfig {

    /**
     * 配置支持websocket
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        log.info("启动websocket支持");
        return new ServerEndpointExporter();
    }

    /**
     * 消息监听器适配器，绑定消息处理器
     *
     * @return
     */
    @Bean
    public MessageListenerAdapter listenerAdapter() {
        return new MessageListenerAdapter(new RedisListener());
    }

    /**
     * 配置redis监听
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //订阅了主题 webSocketMsgPush
        container.addMessageListener(listenerAdapter, new PatternTopic(RedisConstants.REDIS_CHANNEL));
        return container;
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2024/6/17 Leo creat
 */