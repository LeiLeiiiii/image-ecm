package com.sunyard.module.system.api;

import javax.annotation.Resource;

import com.sunyard.framework.message.service.SSEMessageSentService;
import com.sunyard.framework.redis.util.RedisUtils;
import org.dromara.email.api.MailClient;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.message.util.EmailUtils;
import com.sunyard.module.system.api.dto.MessageDTO;
import com.sunyard.module.system.mapper.SysMsgMapper;
import com.sunyard.module.system.po.SysMsg;

import cn.hutool.core.lang.Assert;

import java.util.Set;

/**
 * @author huronghao
 * @Type
 * @Desc
 * @date 2023-05-30 9:53
 */
@RestController
public class MessageApiImpl implements MessageApi {
    @Resource
    private SysMsgMapper sysMsgMapper;
    @Resource
    private SSEMessageSentService sseMessageSentService;
    @Resource
    private EmailUtils email;
    @Resource
    private RedisUtils redisUtils;

    @Override
    public Result<Boolean> saveMsgJson(MessageDTO dto) {
        Assert.notNull(dto.getMsgSystem(),"参数错误");
        Assert.notNull(dto.getMsgType(),"参数错误");
        Assert.notNull(dto.getMsgHead(),"参数错误");
        Assert.notNull(dto.getMsgContent(),"参数错误");
        Assert.notNull(dto.getAcceptUser(),"参数错误");
        Assert.notNull(dto.getInformTime(),"参数错误");
        SysMsg sysMsg = new SysMsg();
        BeanUtils.copyProperties(dto, sysMsg);
        sysMsgMapper.insert(sysMsg);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> sendMsgJson(MessageDTO dto) {
        String acceptUser = "myMessage:"+dto.getMsgSystem()+dto.getAcceptUser().toString();
        Set<String> clients= redisUtils.stringKeys(acceptUser);
        for(String client:clients){
            sseMessageSentService.sendMessage(client,()->dto);
        }
        return Result.success(true);
    }

    @Override
    public Result<Boolean> saveAndSendMsgJson(MessageDTO dto) {
        saveMsgJson(dto);
        String acceptUser = "myMessage:"+dto.getMsgSystem()+dto.getAcceptUser().toString();
        Set<String> clients= redisUtils.stringKeys(acceptUser);
        for(String client:clients){
            sseMessageSentService.sendMessage(client,()->dto);
        }
        return Result.success(true);
    }

    @Override
    public Result<Boolean> sendFinishMsgAndId(Long userId,String planId) {
        //TODO 待更新
        sseMessageSentService.sendMessage(userId.toString(),()->planId);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> sendEmail(MessageDTO dto) {
        MailClient mailClient = email.getMailClient();
        mailClient.sendMail(dto.getMailAddress(),dto.getMsgHead(),dto.getMsgContent());
        return Result.success(true);
    }

    @Override
    public Result<Boolean> sendEmailBatch(MessageDTO dto) {
        MailClient mailClient = email.getMailClient();
        mailClient.sendMail(dto.getMailAddressList(),dto.getMsgHead(),dto.getMsgContent());
        return Result.success(true);
    }

}
