package com.sunyard.sunafm.service.mq;


import com.sunyard.sunafm.constant.AfmConstant;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


/**
 * 业务逻辑：声明Exchange 和 Queue 消息队列，发送消息的方法
 */
@Service
public class MessageService {
    //AmqpAdmin来管理Exchange、队列和绑定
    private final AmqpAdmin amqpAdmin;

    //AmqpTemplate来发送消息
    private final AmqpTemplate amqpTemplate;

    //通过有参构造器进行依赖注入
    public MessageService(AmqpAdmin amqpAdmin, AmqpTemplate amqpTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.amqpTemplate = amqpTemplate;

        DirectExchange exchange = new DirectExchange(
                AfmConstant.EXCHANGE_NAME,
                true,    /* Exchange是否持久化 */
                false, /* 是否自动删除 */
                null   /* 额外的参数属性 */);
        //声明 Exchange
        this.amqpAdmin.declareExchange(exchange);


        //此处循环声明 Queue ，也相当于代码式创建 Queue
        Queue queue = new Queue(AfmConstant.QUEUE_NAMES,   /* Queue 消息队列名 */
                true,         /* 是否是持久的消息队列 */
                false,       /* 是否是独占的消息队列，独占就是是否只允许该消息消费者消费该队列的消息 */
                false,     /* 是否在没有消息的时候自动删除消息队列 */
                null       /* 额外的一些消息队列的参数 */
        );
        //此处声明 Queue ，也相当于【代码式】创建 Queue
        this.amqpAdmin.declareQueue(queue);

        //声明 Queue 的绑定
        Binding binding = new Binding(
                AfmConstant.QUEUE_NAMES,  /* 指定要分发消息目的地的名称--这里是要发送到这个消息队列里面去 */
                Binding.DestinationType.QUEUE, /* 分发消息目的的类型，指定要绑定 queue 还是 Exchange */
                AfmConstant.EXCHANGE_NAME, /* 要绑定的Exchange */
                "yx", /* 因为绑定的Exchange类型是 fanout 扇形（广播）模式，所以路由key随便写，没啥作用 */
                null
        );
        //声明 Queue 的绑定
        amqpAdmin.declareBinding(binding);
    }

    /**
     * 发送消息的方法
     */
    public void publish(String content) {
        // 设置TTL为10分钟
        long ttl = TimeUnit.MINUTES.toMillis(AfmConstant.FAILURE_TIME_MESSAGE);

        // 设置消息的TTL
        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance()
                .setExpiration(String.valueOf(ttl))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT) // 确保消息是持久的
                .build();

        // 创建消息对象
        Message message = new Message(content.toString().getBytes(), messageProperties);

        //发送消息
        amqpTemplate.convertAndSend(
                /* 指定将消息发送到这个Exchange */
                AfmConstant.EXCHANGE_NAME,
                /* 因为Exchange是fanout 类型的（广播类型），所以写什么路由key都行，都没意义 */
                "yx",
                /* 发送的消息体 */
                message,
                m -> {
                    m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return m;
                }
        );
    }
}
