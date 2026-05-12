package com.sunyard.ecm.websocket;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.sunyard.ecm.manager.FileInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.List;

/**
 * @author yzy
 * @desc 处理websocket消息推送与接收处理
 * @since 2025/3/20
 */
@Component
@Slf4j
public class WebSocketService {


    /**
     * 推送消息
     */
    public void pushMsg(WebSocketMessageDTO dto){
        WebSocketServer server = SpringUtil.getBean(WebSocketServer.class);
        WebApiSocketServer serverApi = SpringUtil.getBean(WebApiSocketServer.class);
        WebSocketMessageDTO msg=new WebSocketMessageDTO();
        BeanUtils.copyProperties(dto,msg);
        msg.setContentText(JSON.toJSONString(dto));
        server.sendMessageToBuisIdList(msg);
        serverApi.sendMessageToBuisIdList(msg);
    }
    /**
     * 根据会话推送消息
     */
    public void pushMsg(WebSocketMessageDTO dto,Session session){
        WebSocketServer server = SpringUtil.getBean(WebSocketServer.class);
        WebApiSocketServer serverApi = SpringUtil.getBean(WebApiSocketServer.class);
        String msg=JSON.toJSONString(dto);
        server.sendOneMessage(session,msg);
//        serverApi.sendOneMessage(session,msg);
    }

    /**
     * 根据会话推送异常消息
     */
    public void pushError(Session session,String errorMsg){
        WebSocketMessageDTO dto=new WebSocketMessageDTO();
        dto.setContentText("获取失败,异常信息:"+errorMsg);
        WebSocketServer server = SpringUtil.getBean(WebSocketServer.class);
        WebApiSocketServer serverApi = SpringUtil.getBean(WebApiSocketServer.class);
        String msg=JSON.toJSONString(dto);
        server.sendOneMessage(session,msg);
        serverApi.sendOneMessage(session,msg);
    }

    /**
     * 分发客户端的消息
     */
    public void processMessage(String message, Session session){
        try{
            WebSocketMessageDTO dto=JSON.parseObject(message,WebSocketMessageDTO.class);
            WebSocketMsgTypeEnum msgType = WebSocketMsgTypeEnum.fromDescription(dto.getMsgType());
            String data=dto.getContentText();
            List<String> busiIds=dto.getBuisIdList();
            if (msgType != null) {
                switch (msgType){
                    case AUTO_CLASS_NUM:
                        pushAutoClassNum(busiIds,session);
                        break;
                }
            }
        }catch (Exception e){
            pushError(session,e.getMessage());
            log.error("websocket消息处理异常:",e);
        }
    }


    /**
     * AUTO_CLASS_NUM
     */
    public void pushAutoClassNum(List<String> busiIds,Session session){
        FileInfoService fileInfoService =SpringUtil.getBean(FileInfoService.class);
        Integer num=((List<?>) fileInfoService.getCheckAutoGroupList(Long.valueOf(busiIds.get(0))).getData()).size();
        WebSocketMessageDTO dto=new WebSocketMessageDTO();
        dto.setMsgType(WebSocketMsgTypeEnum.AUTO_CLASS_NUM.description());
        dto.setBuisIdList(busiIds);
        dto.setContentText(String.valueOf(num));
        pushMsg(dto,session);
    }
}
