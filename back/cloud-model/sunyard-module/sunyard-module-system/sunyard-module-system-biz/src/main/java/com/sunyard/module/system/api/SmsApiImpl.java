package com.sunyard.module.system.api;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SmsDTO;
import com.sunyard.module.system.api.dto.SmsQuerySmsStatusRequestDTO;
import com.sunyard.module.system.api.dto.SmsSendBatchSmsRequestDTO;
import com.sunyard.module.system.mapper.SysSmsMapper;
import com.sunyard.module.system.po.SysSmsLog;
import com.sunyard.module.system.sms.bo.QuerySmsStatusReponse;
import com.sunyard.module.system.sms.bo.QuerySmsStatusRequest;
import com.sunyard.module.system.sms.bo.SendBatchSmsReponse;
import com.sunyard.module.system.sms.bo.SendBatchSmsRequest;
import com.sunyard.module.system.sms.bo.SendSmsReponse;
import com.sunyard.module.system.sms.bo.SendSmsRequest;
import com.sunyard.module.system.sms.service.SmsService;

import cn.hutool.core.date.DateUtil;


/**
 * @author zhaojianmou
 * @Desc
 * @date 2025/8/25 15:34
 */
@RestController
public class SmsApiImpl implements SmsApi{
    @Resource
    private SmsService service;
    @Resource
    private SysSmsMapper sysSmsMapper;

    /**
     * 根据模板发送短信（一对一发送）
     * @param sysSmsDTO
     * @return
     */
    @Override
    public Result sendSms(SmsDTO sysSmsDTO) {
        SendSmsRequest request = new SendSmsRequest();
        request.setTemplateNum(sysSmsDTO.getTemplateNum());
        request.setParamMap(sysSmsDTO.getParamMap());
        request.setMobile(sysSmsDTO.getTelePhone());
        request.setSendTime(DateUtil.now());
        SendSmsReponse sendSmsReponse = service.sendSms(request);
        //记录到表中
        recordSmsLog(JSON.toJSONString(sendSmsReponse),sendSmsReponse.getCode(),
                sendSmsReponse.getMsg(),JSON.toJSONString(sysSmsDTO));
        if(sendSmsReponse.getCode().equals("000000")){
            return Result.success();
        }else {
            return Result.error(sendSmsReponse.getMsg(),500);
        }
    }

    /**
     * 根据模板发送短信（批量）
     * @param smsSendBatchSmsRequestDTO
     * @return
     */
    @Override
    public Result sendBatchSms(SmsSendBatchSmsRequestDTO smsSendBatchSmsRequestDTO) {
        SendBatchSmsRequest sendBatchSmsRequest=new SendBatchSmsRequest();
        BeanUtils.copyProperties(smsSendBatchSmsRequestDTO,sendBatchSmsRequest);
        sendBatchSmsRequest.setSendTime(DateUtil.now());
        SendBatchSmsReponse sendBatchSmsReponse = service.sendBatchSms(sendBatchSmsRequest);
        //记录到表中
        recordSmsLog(JSON.toJSONString(sendBatchSmsReponse),sendBatchSmsReponse.getCode(),
                sendBatchSmsReponse.getMsg(),JSON.toJSONString(smsSendBatchSmsRequestDTO));
        if(sendBatchSmsReponse.getCode().equals("000000")){
            return Result.success();
        }else {
            return Result.error(sendBatchSmsReponse.getMsg(),500);
        }
    }

    /**
     * 短信状态查询
     * @param smsStatusRequestDTO
     * @return
     */
    @Override
    public Result  querySmsStatus(SmsQuerySmsStatusRequestDTO smsStatusRequestDTO) {
        QuerySmsStatusRequest request = new QuerySmsStatusRequest();
        BeanUtils.copyProperties(smsStatusRequestDTO,request);
        QuerySmsStatusReponse querySmsStatusReponse = service.querySmsStatus(request);
        //记录到表中
        recordSmsLog(JSON.toJSONString(querySmsStatusReponse),querySmsStatusReponse.getCode(),
                querySmsStatusReponse.getMsg(),JSON.toJSONString(smsStatusRequestDTO));
        if(querySmsStatusReponse.getCode().equals("000000")){
            return Result.success();
        }else {
            return Result.error(querySmsStatusReponse.getMsg(),500);
        }
    }

    /**
     * 短信日志记录
     * @param responseContent 短信响应内容
     * @param code 响应状态码
     * @param msg 响应信息
     * @param requestContent 请求内容
     */
    @Async("LogThreadPool")
    public void recordSmsLog(String responseContent, String code, String msg,String requestContent) {
        SysSmsLog sysSmsLog = new SysSmsLog();
        sysSmsLog.setResponseContent(responseContent);
        sysSmsLog.setSmsCode(code);
        sysSmsLog.setSmsMsg(msg);
        sysSmsLog.setRequestContent(requestContent);
        sysSmsMapper.insert(sysSmsLog);
    }
}
