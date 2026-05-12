package com.sunyard.framework.common.util;
/*
 * Project: com.sunyard.am.utils
 *
 * File Created at 2021/7/1
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.utils
 * @Desc
 * @date 2021/7/1 16:06
 */
public final class RegexpUtils {

    // ------------------常量定义
    /**
     * Email正则表达式="^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
     */
    public static final String EMAIL =
        "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    /**
     * 手机号码正则表达式=^(13[0-9]|14[0-9]|15[0-9]|17[0-9]|18[0-9])\d{8}$
     */
    public static final String MOBILE = "^(13[0-9]|14[0-9]|15[0-9]|17[0-9]|18[0-9]|19[0-9])\\d{8}$";
    /**
     * 验证手机号与座机号
     */
    public static final String MOBILEORPHONE = "(^(0[0-9]{2,3}\\-)?([2-9][0-9]{6,7})+"
        + "(\\-[0-9]{1,4})?$)$|(^(13[0-9]|14[0-9]|15[0-9]|17[0-9]|18[0-9]|19[0-9])\\d{8}$)";

    /**
     * 匹配由数字、26个英文字母或者下划线组成的字符串 ^\w+$
     */
    public static final String STR_ENG_NUM_UNDERSCORE = "^\\w+$";

    /**
     * 过滤特殊字符串正则 regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]";
     */
    public static final String STR_SPECIAL = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]";

    /**
     * URL正则表达式 匹配 http www ftp
     */
    public static final String URL =
        "^(http|www|ftp|)?(://)?(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*((:\\d+)?)(/(\\w+(-\\w+)*))*(\\.?(\\w)*)(\\?)?"
            + "(((\\w*%)*(\\w*\\?)*(\\w*:)*(\\w*\\+)*(\\w*\\.)*(\\w*&)*(\\w*-)*(\\w*=)*(\\w*%)*(\\w*\\?)*"
            + "(\\w*:)*(\\w*\\+)*(\\w*\\.)*" + "(\\w*&)*(\\w*-)*(\\w*=)*)*(\\w*)*)$";

    /**
     * 匹配是否中文/英文大小写/数字
     */
    public static final String CHINESE_ENG_NUM = "^[\\w\\u4E00-\\u9FA5\\uF900-\\uFA2D]*$";

    /**
     * 匹配是否为中文字符
     */
    public static final String IS_CHINESE = "[\\u4E00-\\u9FA5\\uF900-\\uFA2D]+";

    /**
     * 匹配是否为数字字符
     */
    public static final String IS_NUMBER = "[0-9]";
    /**
     * 匹配是否为英文字符
     */
    public static final String IS_EN = "[a-zA-z]";

    /**
     * 密码规则校验
     * @param str  密码
     * @return Result
     */
    public static synchronized boolean isPasswordLegal(String str) {
        String password = str.trim();
        Pattern p = Pattern.compile(IS_CHINESE);
        Matcher m = p.matcher(password);
        boolean hasChinese = m.matches();
        if (hasChinese) {
            return false;
        }
        // 判断是否包含英文
        p = Pattern.compile(".*" + IS_EN + "+.*");
        m = p.matcher(password);
        boolean hasEng = m.matches();

        // 判断是否包含数字
        p = Pattern.compile(".*" + IS_NUMBER + "+.*");
        m = p.matcher(password);
        boolean hasNum = m.matches();

        // 判断是否包含特殊字符
        p = Pattern.compile(".*" + STR_SPECIAL + ".*");
        m = p.matcher(password);
        boolean hasSpecial = m.matches();
        if (password.length() < 6 || password.length() > 16) {
            return false;
        }
        // 三者中有两个就为true
        return ((hasNum ? 1 : 0) + (hasEng ? 1 : 0) + (hasSpecial ? 1 : 0) >= 2);
    }

    /**
     * 判断字段是否为Email 符合返回ture
     * 
     * @param str 邮箱
     * @return Result boolean
     */
    public static boolean isEmail(String str) {
        return regular(str, EMAIL);
    }

    /**
     * 判断是否为手机号码 符合返回ture
     * 
     * @param str 手机号
     * @return Result boolean
     */
    public static boolean isMobile(String str) {
        return regular(str, MOBILE);
    }

    /**
     * 判断是否为手机号码或座机号 符合返回ture
     * 
     * @param str 手机号
     * @return Result boolean
     */
    public static boolean isMobileOrPhone(String str) {
        return regular(str, MOBILEORPHONE);
    }

    /**
     * 判断字符串是不是全部是英文字母+数字+下划线
     * 
     * @param str 字符串
     * @return Result boolean
     */
    public static boolean isEngNumUnderscore(String str) {
        return regular(str, STR_ENG_NUM_UNDERSCORE);
    }

    /**
     * 判断字符串是不是全部是中文+英文字母+数字
     * 
     * @param str 字符串
     * @return Result boolean
     */
    public static boolean isEngNumChinese(String str) {
        return regular(str, CHINESE_ENG_NUM);
    }

    /**
     * 匹配是否符合正则表达式pattern 匹配返回true
     * 
     * @param str 匹配的字符串
     * @param pattern 匹配模式
     * @return Result boolean
     */
    private static boolean regular(String str, String pattern) {
        if (null == str || str.trim().length() <= 0) {
            return false;
        }
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        return m.matches();
    }

}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/1 zhouleibin creat
 */
