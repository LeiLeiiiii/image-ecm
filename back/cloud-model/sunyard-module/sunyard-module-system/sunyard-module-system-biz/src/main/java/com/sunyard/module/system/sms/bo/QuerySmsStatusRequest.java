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

import lombok.Data;

/**
 * @Desc 短信状态查询 请求对象
 * @author Leo
 * @date 2025/8/15 10:23
 * @version
 */
@Data
public class QuerySmsStatusRequest extends SmsBaseRequest{
    /**  手机号: 接收短信的手机号码。格式：11位手机号码 国际区号+号码 */
    private String phoneNumber;
    /**  回执消息id: 发送回执id 调用发送接口SendSms或SendBatchSms发送短信时，返回值中的msgId字段 */
    private String bizId;
    /** 短信发送日期: 短信发送日期格式为yyyyMMdd*/
    private String sendTime;
    /** 每页数量: 分页查看发送记录，每页显示的数量 */
    private String pageSize;
    /** 当前页码:  分页查看发送记录，制定发送记录的当前页码*/
    private String currentPage;


    @Override
    public String getUrl() {
        return getBaseUrl()+"/querySmsStatus";
    }

}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/15 Leo creat
 */
