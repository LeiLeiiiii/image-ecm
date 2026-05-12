package com.sunyard.framework.message.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyard.framework.message.config.properties.MessageProperties;
import com.sunyard.framework.redis.util.RedisUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * sse推送信息
 *
 * @author zyh
 * @date 2022.2.13 16:01
 */
@Slf4j
@Component
public class SSEMessageSentService {
    @Resource
    private MessageProperties messageProperties;
    @Resource(name = "MsgThreadPool")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private RedisMessageListenerContainer redisMessageListenerContainer;
    @Resource
    private RedisUtils redisUtils;
    // Redis中存储连接信息的key前缀
    private String SSE_CONNECTION_PREFIX;
    // Redis广播频道名称
    private String SSE_BROADCAST_CHANNEL;
    // JSON序列化工具
    private final ObjectMapper objectMapper = new ObjectMapper();
    // 本地连接存储，key: clientId, value: SseEmitter
    private final Map<String, SseEmitter> clientConnections = new ConcurrentHashMap<>();
    //存储任务
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    //调度单线程
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 初始化Redis监听器
     */
    @PostConstruct
    public void initRedisListener() {
        // 实际初始化连接前缀和广播频道
        this.SSE_CONNECTION_PREFIX = messageProperties.getAppName() + ":sse:connection:";
        this.SSE_BROADCAST_CHANNEL = messageProperties.getAppName() + ":sse:broadcast";

        // 创建Redis消息监听器
        MessageListener messageListener = new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                try {
                    String messageBody = new String(message.getBody());
                    BroadcastMessage broadcastMessage = objectMapper.readValue(messageBody, BroadcastMessage.class);
                    handleBroadcastMessage(broadcastMessage.getClientId(), broadcastMessage.getMessage());
                } catch (Exception e) {
                    // 处理JSON序列化异常、Redis连接异常、消息处理异常等
                    log.error("[SSE] 处理Redis广播消息失败, channel: {}", SSE_BROADCAST_CHANNEL, e);
                }
            }
        };
        // 订阅广播
        redisMessageListenerContainer.addMessageListener(messageListener, new ChannelTopic(SSE_BROADCAST_CHANNEL));
    }

    /**
     * 创建连接
     *
     * @param clientId 客户端ID
     * @return SseEmitter对象
     * @throws IllegalStateException 当clientId已存在时抛出异常
     */
    public SseEmitter createConnection(String clientId) {
        // 检查本地是否已存在该clientId的连接
        if (clientConnections.containsKey(clientId)) {
            log.warn("[SSE] 客户端ID已存在，将先关闭旧连接. clientId: {}", clientId);
            SseEmitter oldEmitter = clientConnections.get(clientId);
            if (oldEmitter != null) {
                try {
                    oldEmitter.complete();
                } catch (Exception e) {
                    // 处理IllegalStateException(连接已完成或超时)、IOException(网络IO异常)等
                    log.error("[SSE] 关闭旧连接失败. clientId: {}", clientId, e);
                }
            }
            // 移除旧连接
            removeConnection(clientId);
        }
        // 检查Redis中是否已存在该clientId的连接
        Boolean exists = redisUtils.hasKey(SSE_CONNECTION_PREFIX + clientId);
        if (Boolean.TRUE.equals(exists)) {
            log.warn("[SSE] Redis中已存在客户端ID，将先删除旧连接记录. clientId: {}", clientId);
            // 删除Redis中的旧连接记录
            redisUtils.del(SSE_CONNECTION_PREFIX + clientId);
        }
        // 设置 1 小时的 Emitter 超时
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        // 保存本地连接
        clientConnections.put(clientId, emitter);
        // Redis 设置过期时间略大于超时
        redisUtils.opsForValueAndSet(
                SSE_CONNECTION_PREFIX + clientId,
                String.valueOf(System.currentTimeMillis()),
                65 * 60, TimeUnit.SECONDS
        );

        log.info("[SSE] 客户端连接成功. clientId: {}, 当前在线数: {}", clientId, clientConnections.size());

        // 连接关闭/超时/出错时，移除无效连接
        emitter.onCompletion(() -> removeConnection(clientId));
        emitter.onTimeout(() -> removeConnection(clientId));
        emitter.onError(ex -> removeConnection(clientId));

        // 立即发送连接确认 + 心跳
        try {
            emitter.send(SseEmitter.event().comment("connected"));
            emitter.send(SseEmitter.event().name("heartbeat").comment("keep-alive"));
        } catch (IOException e) {
            removeConnection(clientId);
        }

        return emitter;
    }

    /**
     * 分布式发送消息
     *
     * @param clientId 客户端ID
     * @param message  要发送的消息
     * @return true表示发送成功，false表示发送失败（连接不存在）
     */
    public boolean sendMessageDistributed(String clientId, Object message) {
        Boolean exists = redisUtils.hasKey(SSE_CONNECTION_PREFIX + clientId);

        // 检查本地是否存在该连接
        SseEmitter emitter = clientConnections.get(clientId);
        if (emitter != null) {
            // 本地存在，直接发送
            try {
                emitter.send(message);
                return true;
            } catch (IOException e) {
                // 处理网络IO异常、客户端连接中断等
                removeConnection(clientId);
                return false;
            }
        } else {
            // 本地不存在，广播消息
            if (Boolean.FALSE.equals(exists)) {
                return false;
            }
            broadcastMessage(clientId, message);
            return true;
        }
    }

    /**
     * 使用线程池发送SSE消息
     *
     * @param clientId     客户端ID
     * @param dataSupplier 数据提供者
     * @return Future对象，可用于获取发送结果
     */
    public Future<Boolean> sendMessage(String clientId, Supplier<Object> dataSupplier) {
        return threadPoolTaskExecutor.submit(() -> {
            if (sendMessageDistributed(clientId, dataSupplier.get())) {
                return true;
            }
            // 发送失败（Redis中无记录），本地也不会有对应连接，直接返回false
            log.debug("[SSE] 消息发送失败，客户端连接不存在. clientId: {}", clientId);
            return false;
        });
    }

    /**
     * 广播消息到所有节点
     *
     * @param clientId 客户端ID
     * @param message  要发送的消息
     */
    private void broadcastMessage(String clientId, Object message) {
        try {
            BroadcastMessage broadcastMessage = new BroadcastMessage(clientId, message);
            String jsonMessage = objectMapper.writeValueAsString(broadcastMessage);
            redisUtils.convertAndSend(SSE_BROADCAST_CHANNEL, jsonMessage);
        } catch (Exception e) {
            // 处理JSON序列化异常、Redis连接异常、网络IO异常等
            log.error("[SSE] 广播消息失败. clientId: {}", clientId, e);
        }
    }

    /**
     * 处理广播消息（其他节点调用）
     *
     * @param clientId 客户端ID
     * @param message  要发送的消息
     * @return true表示发送成功，false表示发送失败
     */
    public boolean handleBroadcastMessage(String clientId, Object message) {
        SseEmitter emitter = clientConnections.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(message);
                return true;
            } catch (IOException e) {
                //处理网络IO异常、客户端连接中断等
                removeConnection(clientId);
                return false;
            }
        }
        return false;
    }

    /**
     * 移除连接
     *
     * @param clientId 客户端ID
     */
    public void removeConnection(String clientId) {
        // 移除连接
        clientConnections.remove(clientId);
        // 删除 Redis Key
        redisUtils.del(SSE_CONNECTION_PREFIX + clientId);
        // 取消定时任务
        cancelScheduledTask(clientId);
        log.info("[SSE] 客户端断开连接. clientId: {}, 当前在线数: {}",
                clientId, clientConnections.size());
    }

    /**
     * 广播消息数据类
     */
    @Data
    public static class BroadcastMessage {
        private String clientId;
        private Object message;

        public BroadcastMessage() {
            // 无参构造器，用于Jackson反序列化
        }

        public BroadcastMessage(String clientId, Object message) {
            this.clientId = clientId;
            this.message = message;
        }
    }

    public void schedulePush(
            String clientId,
            Supplier<Object> dataSupplier,
            long intervalSeconds) {

        // 取消上次任务
        cancelScheduledTask(clientId);

        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            try {
                // 如果连接不存在或 Redis Key 过期则取消
                if (!clientConnections.containsKey(clientId)
                        || Boolean.FALSE.equals(redisUtils.hasKey(SSE_CONNECTION_PREFIX + clientId))) {
                    cancelScheduledTask(clientId);
                    return;
                }

                // 先发送心跳防止 idle 超时
                sendHeartbeat(clientId);

                Object data = dataSupplier.get();
                boolean success = sendMessageDistributed(clientId, data);

                if (!success) {
                    cancelScheduledTask(clientId);
                }
            } catch (Exception e) {
                cancelScheduledTask(clientId);
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);

        scheduledTasks.put(clientId, task);
    }

    private void sendHeartbeat(String clientId) {
        SseEmitter emitter = clientConnections.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("heartbeat").comment("keep-alive"));
            } catch (IOException e) {
                removeConnection(clientId);
            }
        }
    }

    public void cancelScheduledTask(String clientId) {
        ScheduledFuture<?> task = scheduledTasks.get(clientId);
        if (task != null) {
            task.cancel(true);
            scheduledTasks.remove(clientId);
            removeConnection(clientId);
        }
    }
}
