package com.sunyard.module.system.event;

import javax.annotation.Resource;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.sunyard.module.system.api.LogApi;
import com.sunyard.module.system.api.dto.SysApiLogDTO;
import com.sunyard.module.system.api.dto.SysLogDTO;
import com.sunyard.module.system.api.dto.SysLogLoginDTO;
import com.sunyard.module.system.po.SysMsg;
import com.sunyard.module.system.service.MessageService;

/**
 * @author 周磊斌
 */
@Component
public class LogEventListener {

    @Resource
    private LogApi logApi;

    @Resource
    private MessageService messageService;


    /**
     * 异步记录系统日志
     * @param log 日志obj
     */
    @Async("LogThreadPool")
    @EventListener(value = SysLogDTO.class)
    public void saveLog(SysLogDTO log) {
        logApi.add(log);
    }

    /**
     * 异步记录接口日志
     * @param log 日志obj
     */
    @Async("LogThreadPool")
    @EventListener(value = SysApiLogDTO.class)
    public void saveApiLog(SysApiLogDTO log) {
        logApi.addSysApiLog(log);
    }

    /**
     * 异步记录登录日志
     * 
     * @param log 日志obj
     */
    @Async("LogThreadPool")
    @EventListener(value = SysLogLoginDTO.class)
    public void saveLoginLog(SysLogLoginDTO log) {
        logApi.addLogin(log);
    }

    /**
     * 消息监听
     *
     * @param message ArcMsgListener
     */
    @Async("GlobalThreadPool")
    @EventListener(value = SysMsg.class)
    public void arcFlowLog(SysMsg message) {
        messageService.addMessage(message);
    }

}
