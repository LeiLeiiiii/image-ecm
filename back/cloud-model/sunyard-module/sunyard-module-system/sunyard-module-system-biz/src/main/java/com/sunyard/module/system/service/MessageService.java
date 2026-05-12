package com.sunyard.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.message.service.SSEMessageSentService;
import com.sunyard.module.system.enums.table.SmsStatusEnum;
import com.sunyard.module.system.mapper.SysMsgMapper;
import com.sunyard.module.system.po.SysMsg;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

/**
 * @author zhaoyang 2021/10/21 17:32
 */
@Service
public class MessageService {

    @Resource
    private SysMsgMapper sysMsgMapper;
    @Resource
    private SSEMessageSentService sseMessageSentService;

    /**
     * 添加消息
     *
     * @param message 消息对象
     * @return Result
     */
    public Result addMessage(SysMsg message) {
        AssertUtils.isNull(message, "参数错误");
        sysMsgMapper.insert(message);
        return Result.success(true);
    }

    /**
     * 我的消息未读消息数量
     *
     * @param userId 用户id
     * @return Result
     */
    public Result myCount(Long userId, String systemType) {
        Assert.notNull(userId, "参数错误");
        Long count = sysMsgMapper
                .selectCount(new LambdaQueryWrapper<SysMsg>()
                        .eq(SysMsg::getAcceptUser, userId)
                        .eq(StringUtils.hasText(systemType), SysMsg::getMsgSystem, systemType)
                        .eq(SysMsg::getMsgStatus, SmsStatusEnum.UNREAD.getCode()));
        return Result.success(count);
    }

    /**
     * 我的消息列表 全部消息展示
     *
     * @param token
     * @param systemType
     * @return {@link SseEmitter }
     */
    public SseEmitter search(AccountToken token, String systemType) {
        Assert.notNull(token.getId(), "参数错误");
        //SSE推送消息
        String clientId = "myMessage:" + systemType + token.getId() + UUID.randomUUID();
        SseEmitter emitter = sseMessageSentService.createConnection(clientId);

        // 使用 Supplier 每次重新查询数据库
        sseMessageSentService.schedulePush(clientId, () -> {
            // 每次推送时重新查询最新的消息
            List<SysMsg> latestMessageList = sysMsgMapper.selectList(new LambdaQueryWrapper<SysMsg>()
                    .eq(SysMsg::getAcceptUser, token.getId())
                    .eq(StringUtils.hasText(systemType), SysMsg::getMsgSystem, systemType)
                    .orderByDesc(SysMsg::getCreateTime));
            return latestMessageList;
        }, 20);
        return emitter;
    }

    /**
     * 修改阅读状态
     *
     * @param id id
     * @return Result
     */
    public Result changeStatus(Long id) {
        Assert.notNull(id, "参数错误");
        sysMsgMapper.update(null, new LambdaUpdateWrapper<SysMsg>().eq(SysMsg::getMsgId, id)
                .set(SysMsg::getMsgStatus, SmsStatusEnum.READ.getCode()));
        return Result.success(true);
    }

    /**
     * 一键已读 修改全部阅读状态
     *
     * @param userId 用户id
     * @return Result
     */
    public Result changeAllStatus(Long userId) {
        Assert.notNull(userId, "参数错误");
        sysMsgMapper.update(null, new LambdaUpdateWrapper<SysMsg>()
                .eq(SysMsg::getAcceptUser, userId).set(SysMsg::getMsgStatus, SmsStatusEnum.READ.getCode()));
        return Result.success(true);
    }

}
