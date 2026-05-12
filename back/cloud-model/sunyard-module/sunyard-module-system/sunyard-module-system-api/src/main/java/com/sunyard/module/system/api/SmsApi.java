package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SmsQuerySmsStatusRequestDTO;
import com.sunyard.module.system.api.dto.SmsSendBatchSmsRequestDTO;
import com.sunyard.module.system.api.dto.SmsDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author zhaojianmou
 * @Desc
 * @date 2025/8/25 15:26
 */
@FeignClient(value = ApiConstants.NAME)
public interface SmsApi {
    String PREFIX = ApiConstants.PREFIX + "/sms/";

    /**
     * 根据模板发送短信（一对一发送）
     * @param sysSmsDTO
     * @return
     */
    @PostMapping(PREFIX + "sendSms")
    Result sendSms(@RequestBody SmsDTO sysSmsDTO);

    /**
     * 根据模板发送短信（批量）
     * @param smsSendBatchSmsRequestDTO
     * @return
     */
    @PostMapping(PREFIX + "sendBatchSms")
    Result  sendBatchSms(@RequestBody SmsSendBatchSmsRequestDTO smsSendBatchSmsRequestDTO);

    /**
     * 短信状态查询
     * @param smsStatusRequestDTO
     * @return
     */
    @PostMapping(PREFIX + "querySmsStatus")
    Result querySmsStatus(@RequestBody SmsQuerySmsStatusRequestDTO smsStatusRequestDTO);
}
