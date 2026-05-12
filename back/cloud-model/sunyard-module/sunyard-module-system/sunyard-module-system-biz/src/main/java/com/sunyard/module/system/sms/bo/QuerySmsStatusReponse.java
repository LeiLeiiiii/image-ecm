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
 * @Desc 短信状态查询 返回对象
 * @author Leo
 * @date 2025/8/15 10:23
 * @version
 */
@Data
public class QuerySmsStatusReponse extends SmsBaseReponse {
    private static final long serialVersionUID = 1403758798124374538L;

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
        /**
         * 返回码:0成功,3失败
         * */
        private String code;
        /** 状态码描述 */
        private String message;
        /** 短信发送总条数 */
        private String totalCount;
        /** 请求id */
        private String requestId;

        /** 错误详情	errorMsg,错误码（成功返回空） */
        private String errorMsg;
        /** 响应数据 */
        private String responseData;
        /** 响应数据 */
        private List<SmsSendDetailDTO> smsSendDetailDTOs;

    }

}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/15 Leo creat
 */
