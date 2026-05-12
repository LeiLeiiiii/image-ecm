package com.sunyard.module.system.controller;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.MessageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

/**
 * 工作台/消息提醒
 *
 * @author zhaoyang 2021/10/21 17:48
 */
@RestController
@RequestMapping("global/message")
public class GlobalBasicsCurrencyMessageController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.GLOBAL_MESSAGE + "->";
    @Resource
    private MessageService messageService;

    /**
     * 查询未读消息
     * @return Result
     */
    @GetMapping(value = "searchMessage", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter searchMessage(String systemType) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no"); // Nginx 关键
        response.setHeader("Connection", "keep-alive");
        return messageService.search(getToken(),systemType);
    }

    /**
     * 未读数量
     * @return Result
     */
    @OperationLog(BASELOG + "查询消息未读数量")
    @PostMapping("myCount")
    public Result myCount(String systemType) {
        return messageService.myCount(getToken().getId(),systemType);
    }

    /**
     * 已读
     * @param id 消息id
     * @return result
     */
    @PostMapping("changeStatus")
    @OperationLog(BASELOG + "已读")
    public Result changeStatus(Long id) {
        return messageService.changeStatus(id);
    }

    /**
     * 一键已读
     * @return result
     */
    @OperationLog(BASELOG + "一件已读")
    @PostMapping("changeAllStatus")
    public Result changeAllStatus() {
        return messageService.changeAllStatus(getToken().getId());
    }
}
