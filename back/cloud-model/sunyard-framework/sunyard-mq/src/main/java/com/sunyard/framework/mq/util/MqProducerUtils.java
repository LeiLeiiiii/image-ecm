package com.sunyard.framework.mq.util;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author liuwen
 * @Description: rabbitMQ消息发送工具类
 * @Date: 2023/8/24
 */
public class MqProducerUtils {

    private String queueName;
    private String exchangeName;

    public MqProducerUtils(String queueName, String exchangeName) {
        this.queueName = queueName;
        this.exchangeName = exchangeName;
    }

    /**
     * 发送消息
     * @param factory 连接工厂
     * @param message 消息
     * @throws Exception 异常
     */
    public void sendMessage(ConnectionFactory factory, String message) throws Exception {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 声明队列
            channel.queueDeclare(queueName, false, false, false, null);
            // 声明交换机
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT);
            // 绑定队列和交换机
            channel.queueBind(queueName, exchangeName, "");
            // 设置过期时间为1小时10秒
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .expiration("1000*60*60+10")
                    .build();
            // 发送消息
            channel.basicPublish(exchangeName, "", null, message.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 发送消息
     * @param factory 连接工厂
     * @param message 消息
     * @throws Exception 异常
     */
    public void sendMessageByTime(ConnectionFactory factory, String message,String time) throws Exception {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 声明队列
            channel.queueDeclare(queueName, true, false, false, null);
            // 声明交换机
            channel.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT);
            // 绑定队列和交换机
            channel.queueBind(queueName, exchangeName, "");
            // 设置过期时间为1小时10秒
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .expiration(time)
                    .build();
            // 发送消息
            channel.basicPublish(exchangeName, "", properties, message.getBytes(StandardCharsets.UTF_8));
        }
    }
}




