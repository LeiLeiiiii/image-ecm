package com.sunyard.module.auth.constant;
/*
 * Project: com.sunyard.am.constant
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

/**
 * @author zhouleibin
 * @Type com.sunyard.am.constant
 * @Desc
 * @date 2021/6/30 8:04
 */
public class CachePrefixConstants {

    /** auth-service */
    public static final String AUTH = "auth-service:";
    /** 图片验证码前缀 */
    public static final String PIC_QR_CODE = AUTH + "PIC_QR_CODE:";
    /** 短信验证码前缀 */
    public static final String SMS_CODE = AUTH + "SMS_CODE:";
    /** 短信验证码发送次数前缀 */
    public static final String SMS_CODE_COUNT = AUTH + "SMS_CODE_COUNT:";
    /** 用户密码错误次数缓存key */
    public static final String LOGIN_ACCOUNT_ERROR_LIMIT = AUTH + "LOGIN_ACCOUNT_ERROR_LIMIT:";
    /** ip密码错误次数缓存key */
    public static final String LOGIN_IP_ERROR_LIMIT = AUTH + "LOGIN_IP_ERROR_LIMIT:";
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
