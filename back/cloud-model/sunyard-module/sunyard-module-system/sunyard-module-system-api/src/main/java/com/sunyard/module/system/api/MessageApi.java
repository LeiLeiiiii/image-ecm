package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.MessageDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author huronghao
 * @Type
 * @Desc
 * @date 2023-05-30 9:50
 */
@FeignClient(value = ApiConstants.NAME)
public interface MessageApi {

    String PREFIX = ApiConstants.PREFIX + "/message/";

    /**
     * 保存消息(不发送)
     * @param messageDTO 消息对象
     * @return Result
     */
    @PostMapping(PREFIX + "saveMsgJson")
    Result<Boolean> saveMsgJson(@RequestBody MessageDTO messageDTO);

    /**
     * 发送消息(不保存)
     * @param messageDTO 消息对象
     * @return Result
     */
    @PostMapping(PREFIX + "sendMsgJson")
    Result<Boolean> sendMsgJson(@RequestBody MessageDTO messageDTO);

    /**
     * 保存并发送消息
     * @param messageDTO 消息对象
     * @return Result
     */
    @PostMapping(PREFIX + "saveAndSendMsgJson")
    Result<Boolean> saveAndSendMsgJson(@RequestBody MessageDTO messageDTO);

    /**
     * 发送消息和id
     * @param userId 用户id
     * @param planId 任务id
     * @return Result
     */
    @PostMapping(PREFIX + "sendFinishMsgAndId")
    Result<Boolean> sendFinishMsgAndId(@RequestParam("userId") Long userId,@RequestParam("planId") String planId);

    /**
     * 发送邮件 单人
     * @param messageDTO 消息对象
     * @return Result 是否成功
     */
    @PostMapping(PREFIX + "sendEmail")
    Result<Boolean> sendEmail(@RequestBody MessageDTO messageDTO);

    /**
     * 发送邮件 多人
     *
     * @param messageDTO
     * @return Result
     */
    @PostMapping(PREFIX + "sendEmailBatch")
    Result<Boolean> sendEmailBatch(@RequestBody MessageDTO messageDTO);
}
