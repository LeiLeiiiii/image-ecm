package com.sunyard.framework.redis.constant;
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
 * @date 2021/6/30 8:05
 */
public class TimeOutConstants {
    public static final long ONE_SECONDS = 60L;
    /** 5分钟 */
    public static final long FIVE_MINUTE = 5 * ONE_SECONDS;
    /** 10分钟 */
    public static final long TEN_MINUTE = 10 * ONE_SECONDS;
    /** 30分钟 */
    public static final long THIRTY_MINUTE = 30 * ONE_SECONDS;

    /** 1小时 */
    public static final long ONE_HOURS = 60 * ONE_SECONDS;
    /** 12小时 */
    public static final long TWELVE_HOURS = 12 * ONE_HOURS;

    /** 1天 */
    public static final long ONE_DAY = 24 * ONE_HOURS;
    /** 7天 */
    public static final long SEVEN_DAY = 7 * ONE_DAY;
    /** 15天 */
    public static final long FIFTEEN_DAY = 15 * ONE_DAY;

    /** 一个月 */
    public static long ONE_MONTH = 30 * ONE_DAY;
    /** 三个月 */
    public static long THERR_MONTH = 3 * 30 * ONE_DAY;
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
