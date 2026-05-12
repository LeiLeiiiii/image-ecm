package com.sunyard.ecm.websocket;
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

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.ecm.constant.RedisConstants;
import com.sunyard.framework.ocr.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.nio.charset.StandardCharsets;

/**
 * @author zhouleibin
 * @Desc redis消毒队列 广播形式，监听器
 * @date 2024/6/17 14:05
 */
@Slf4j
public class RedisListener implements MessageListener {

    @Override
    public void onMessage(Message msg, byte[] bytes) {
        String channel = new String(msg.getChannel(), StandardCharsets.UTF_8);
        if (StringUtils.isNotBlank(msg.toString())) {
            if (RedisConstants.REDIS_CHANNEL.endsWith(channel)) {
                log.debug("消息内容:{}", msg);
                WebSocketMessageDTO dto = JSONObject.parseObject(msg.toString(), WebSocketMessageDTO.class);
                WebSocketServer server = SpringUtil.getBean(WebSocketServer.class);
                WebApiSocketServer serverApi = SpringUtil.getBean(WebApiSocketServer.class);
                if ("all".equals(dto.getMsgType())) {
                    server.sendMessageToAll(dto.getContentText());
                } else {
                    server.sendMessageToBuisIdList(dto);
                }
                if ("all".equals(dto.getMsgType())) {
                    serverApi.sendMessageToAll(dto.getContentText());
                } else {
                    serverApi.sendMessageToBuisIdList(dto);
                }
            } else {
                log.warn("消息内容为空，不处理。");
            }
        } else {
            log.warn("消息内容为空，不处理。");
        }
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2024/6/17 Leo creat
 */