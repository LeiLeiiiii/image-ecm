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
 * @Desc 一对一发送 返回对象
 * @author Leo
 * @date 2025/8/15 10:23
 * @version
 */
@Data
public class SendSmsReponse extends SmsBaseReponse {
    /** 接口响应码  
     * 接口统一响应码
     请求成功：000000
     请求失败：100000
     */
    private String code;
    /** 接口响应信息*/
    private String msg;
    /** 接口响应信息*/
    private ReponseData data;

    @Data
    public static class ReponseData {
        /** 响应时间 */
        private String time;
        /** 消息id */
        private String msgId;
        /**
         * 返回码:0成功,3失败
         * */
        private String code;
        /** 错误详情	errorMsg,错误码（成功返回空） */
        private String errorMsg;
        /** 响应数据 */
        private String responseData;
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/15 Leo creat
 */
