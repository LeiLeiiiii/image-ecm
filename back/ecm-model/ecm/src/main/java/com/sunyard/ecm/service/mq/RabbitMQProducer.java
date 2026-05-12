package com.sunyard.ecm.service.mq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.MessageBuilder;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * @author yzy
 * @desc
 * @since 2025/5/22
 */
@Slf4j
@Service
public class RabbitMQProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    // 发送消息
    public void sendMessage(String message,String exchange,String routingKey) {
        Message msg = MessageBuilder.withBody(message.getBytes(StandardCharsets.UTF_8))
                .setHeader("spring_amqp_receivedRoutingKey", routingKey)
                .build();
        rabbitTemplate.send(exchange, routingKey, msg);
        log.info("MQ发送消息: 交换机 = {}, 路由键 = {}, 消息内容 = {}", exchange, routingKey, message);
    }

    /**
     * 发送消息带TTL
     */
    public void sendMessageWithTTL(String message, String exchange, String routingKey, long ttlMillis) {
        // 1. 创建 MessageProperties 并设置 TTL
        MessageProperties properties = new MessageProperties();
        properties.setExpiration(String.valueOf(ttlMillis));

        // 2. 构建消息
        Message msg = MessageBuilder.withBody(message.getBytes(StandardCharsets.UTF_8))
                .andProperties(properties)
                .build();

        // 3. 发送消息
        rabbitTemplate.send(exchange, routingKey, msg);
        log.info("MQ发送消息: 交换机 = {}, 路由键 = {}, TTL = {}ms, 消息内容 = {}",
                exchange, routingKey, ttlMillis, message);
    }
}
