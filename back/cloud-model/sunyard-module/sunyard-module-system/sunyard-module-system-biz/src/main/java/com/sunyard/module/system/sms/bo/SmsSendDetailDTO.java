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
 * @Desc
 * @author Leo
 * @date 2025/8/15 11:23
 * @version
 */
@Data
public class SmsSendDetailDTO {

    /**  运营商短信状态码: 短信发送成功：DELIVERED。短信发送失败：失败错误码请参见错误码*/
    private String errcode;
    /**  短信模板ID */
    private String templateCode;
    /** 外部流水扩展字段 */
    private String outId;
    /** 短信接收日期和时间 */
    private String receiveDate;
    /** 短信发送日期和时间 */
    private String sendDate;
    /** 短信内容 */
    private String content;
    /** 短信发送状态 :1：等待回执。2：发送失败。3：发送成功
     */
    private String sendStatus;
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/15 Leo creat
 */
