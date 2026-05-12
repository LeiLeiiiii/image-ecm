package com.sunyard.module.system.sms.service;
/*
 * Project: SunAM
 *
 * File Created at 2025/8/15
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import javax.annotation.Resource;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.sunyard.module.system.config.properties.SystemSmsProperties;
import com.sunyard.module.system.sms.bo.QuerySmsStatusReponse;
import com.sunyard.module.system.sms.bo.QuerySmsStatusRequest;
import com.sunyard.module.system.sms.bo.SendBatchSmsReponse;
import com.sunyard.module.system.sms.bo.SendBatchSmsRequest;
import com.sunyard.module.system.sms.bo.SendSmsReponse;
import com.sunyard.module.system.sms.bo.SendSmsRequest;
import com.sunyard.module.system.sms.bo.SmsBaseRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * @Desc
 * @author Leo
 * @date 2025/8/15 10:11
 * @version
 */
@Slf4j
@Service
public class SmsService {

    @Resource
    private SystemSmsProperties systemSmsProperties;
    @Resource
    private RestTemplate template;

    /** 根据模板发送短信（一对一发送） */
    public SendSmsReponse sendSms(SendSmsRequest request) {
        return call(request, SendSmsReponse.class);
    }

    /** 根据模板发送短信（批量） */
    public SendBatchSmsReponse sendBatchSms(SendBatchSmsRequest request) {
        return call(request, SendBatchSmsReponse.class);
    }

    /** 短信状态查询 */
    public QuerySmsStatusReponse querySmsStatus(QuerySmsStatusRequest request) {
        return call(request, QuerySmsStatusReponse.class);
    }

    /**
     * @despciption
     * @param request
     * @param responseClass
     * @return T
     */
    private <T> T call(SmsBaseRequest request, Class<T> responseClass) {
        setRequestBaseInfo(request);
        String url = request.getUrl();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("channelId", systemSmsProperties.getChannelId());
        httpHeaders.set("timestamp", request.getTimestamp());
        httpHeaders.set("sign", request.getSign());
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = request.getHttpEntity(httpHeaders);
        try {
            log.debug("调用短信平台接口，请求接口地址：[{}]", url);
            log.info("调用短信平台接口，请求数据：[{}]", JSON.toJSONString(entity));
            ResponseEntity<T> response = template.postForEntity(url, entity, responseClass);
            log.info("调用短信平台接口，应答数据：[{}]", JSON.toJSONString(response));
            return response == null ? null : response.getBody();
        } catch (Exception e) {
            log.error("调用短信接口错误", e);
            return null;
        }
    }

    /**
     *
     * @despciption
     * @param request void
     */
    private void setRequestBaseInfo(SmsBaseRequest request) {
        request.setBaseUrl(systemSmsProperties.getUrlPrefix());
        request.setAppSecret(systemSmsProperties.getAppSecret());
        request.setAppKey(systemSmsProperties.getAppKey());
        request.setTimestamp(String.valueOf(System.currentTimeMillis()));
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/15 Leo creat
 */
