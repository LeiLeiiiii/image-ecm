package com.sunyard.module.system.constant;
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

    /** system-service */
    public final static String SYSTEM = "system-service:";
    /** auth-service */
    public static final String AUTH = "auth-service:";

    /** 用户密码错误次数缓存key */
    public static final String LOGIN_ACCOUNT_ERROR_LIMIT = AUTH+ "LOGIN_ACCOUNT_ERROR_LIMIT:";

    /**
     * 字典表
     */
    public final static String SYS_DICTIONARY = SYSTEM + "sys_dictionary:";

    /**
     * 忘记密码
     */
    public final static String FORGOT_PASS = SYSTEM + "forgot_pass:";
    /**
     * 系统菜单表
     */
    public final static String SYS_MENU = SYSTEM + "sys_menu:";


}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
