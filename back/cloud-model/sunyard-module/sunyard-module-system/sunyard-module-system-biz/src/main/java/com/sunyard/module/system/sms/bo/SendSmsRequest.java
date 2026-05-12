package com.sunyard.module.system.sms.bo;
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

import java.util.HashMap;

import lombok.Data;

/**
 * @Desc 一对一发送 请求对象
 * @author Leo
 * @date 2025/8/15 10:23
 * @version
 */
@Data
public class SendSmsRequest extends SmsBaseRequest {
    /**  短信发送对接渠道模板id */
    private String templateNum;
    /** 发送时间 */
    private String sendTime;
    /**  短信内容 */
    private HashMap<String, String> paramMap;
    /** 手机号码 */
    private String mobile;
    @Override
    public String getUrl() {
        return getBaseUrl() + "/sendSms";
    }

}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/15 Leo creat
 */
