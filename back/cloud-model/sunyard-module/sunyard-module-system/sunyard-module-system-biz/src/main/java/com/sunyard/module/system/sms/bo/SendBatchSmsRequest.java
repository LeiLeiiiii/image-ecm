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

import java.util.List;

import lombok.Data;

/**
 * @Desc 批量发送 请求对象
 * @author Leo
 * @date 2025/8/15 10:23
 * @version
 */
@Data
public class SendBatchSmsRequest extends SmsBaseRequest{

    /**  短信发送对接渠道模板id */
    private String templateNum;
    /**  批次号 */
    private String batchNo;
    /** 发送时间 */
    private String sendTime;
    /** 批量发送短信手机号参数 */
    private List<Detail> details;

    @Override
    public String getUrl() {
        return getBaseUrl()+"/sendBatchSms";
    }

}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/15 Leo creat
 */
