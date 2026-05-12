package com.sunyard.ecm.websocket;

import cn.hutool.extra.spring.SpringUtil;
import com.sunyard.framework.common.exception.SunyardException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author zhouleibin
 */
@Slf4j
@Component
@ServerEndpoint("/ws/triggerQuery/{busiIds}")
public class WebSocketServer {

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 客户端的sessionId
     */
    private String clientId;

    /**
     * 业务批次集合，用来遍历广播消息
     */
    private List<String> busiIdList;

    /**
     * webSockets 用来计数当前jvm下的用户连接数
     */
    private static final CopyOnWriteArraySet<Session> WEBSOCKETS = new CopyOnWriteArraySet<>();

    /**
     * 用来存对应业务批次下的在线连接用户信息
     */
    private static final ConcurrentHashMap<String, HashMap<String, Session>> BUSIIDMAP = new ConcurrentHashMap<String, HashMap<String, Session>>();

    /**
     * 链接成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("busiIds") String busiIds) {
        this.clientId = session.getId();
        this.busiIdList = Arrays.asList(busiIds.split("-"));
        this.session = session;
        if (busiIdList.size() <= 0) {
            return;
        }
        for (String busiId : busiIdList) {
            HashMap<String, Session> sessionMap = null;
            if (BUSIIDMAP.containsKey(busiId)) {
                sessionMap = BUSIIDMAP.get(busiId);
            } else {
                sessionMap = new HashMap<>();
            }
            sessionMap.put(session.getId(), session);
            BUSIIDMAP.put(busiId, sessionMap);
        }
        WEBSOCKETS.add(session);
        log.debug("用户连接:{},当前在线人数为:{},busiIds:{}", session.getId(), WEBSOCKETS.size(), busiIdList.toString());
        this.sendOneMessage(clientId, "连接成功");
    }

    /**
     * 链接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        WEBSOCKETS.remove(session);
        for (String busiId : busiIdList) {
            if (BUSIIDMAP.containsKey(busiId)) {
                HashMap<String, Session> sessionMap = BUSIIDMAP.get(busiId);
                sessionMap.remove(session.getId());
                if (sessionMap.size() == 0) {
                    BUSIIDMAP.remove(busiId);
                }
            }
        }
        log.debug("用户退出:{},当前在线人数为:{}", clientId, WEBSOCKETS.size());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.debug("【websocket消息】收到客户端消息:{}", message);
        WebSocketMessageDTO dto = new WebSocketMessageDTO();
        dto.setClientId(this.clientId);
        dto.setBuisIdList(this.busiIdList);
        dto.setContentText(message);
        WebSocketService webSocketService=SpringUtil.getBean(WebSocketService.class);
        webSocketService.processMessage(message,session);
//        SpringUtil.getBean(StringRedisTemplate.class).convertAndSend(RedisConstants.REDIS_CHANNEL, JSONObject.toJSONString(dto));
    }

    /**
     * 发送错误时的处理
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:" + this.clientId + ",原因:" + error.getMessage());
        error.printStackTrace();
    }

    /**
     * 广播消息，按业务批次广播
     *
     * @param dto
     * @return
     */
    public void sendMessageToBuisIdList(WebSocketMessageDTO dto) {
        List<String> busiIdList = dto.getBuisIdList();
        String msg = dto.getContentText();
        HashMap<String, Session> map = new HashMap<String, Session>();
        for (String busiId : busiIdList) {
            if (BUSIIDMAP.containsKey(busiId)) {
                HashMap<String, Session> sessionMap = BUSIIDMAP.get(busiId);
                for (String key : sessionMap.keySet()) {
                    Session session = sessionMap.get(key);
                    map.put(key, session);
                }
            }
        }
        for (String key : map.keySet()) {
            Session session = map.get(key);
            synchronized (session) {
                if (session != null && session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(msg);
                    } catch (Exception e) {
                        log.error("{}:消息发送异常", session.getId());
                        throw new SunyardException(e.getMessage());
                    }
                }
            }
        }

    }

    /**
     * 广播消息，按业务批次广播
     *
     * @param msg
     * @return
     */
    public void sendMessageToAll(String msg) {
        for (Session session : WEBSOCKETS) {
            synchronized (session) {
                if (session != null && session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(msg);
                    } catch (Exception e) {
                        log.error("{}:消息发送异常", session.getId());
                        throw new SunyardException(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 单点消息 单人
     *
     * @param clientId
     * @param message
     * @return
     */
    public void sendOneMessage(String clientId, String message) {
        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                log.error("{}:消息发送异常", session.getId());
                throw new SunyardException(e.getMessage());
            }
        }
    }

    /**
     * 单点消息 单人
     *
     * @param session
     * @param message
     * @return
     */
    public void sendOneMessage(Session session, String message) {
        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                log.error("{}:消息发送异常", session.getId());
                throw new SunyardException(e.getMessage());
            }
        }
    }

}
