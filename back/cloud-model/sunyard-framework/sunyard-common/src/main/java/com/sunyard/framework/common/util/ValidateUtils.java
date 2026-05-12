package com.sunyard.framework.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * @author liugang
 * @Type com.sunyard.am.utils
 * @Desc
 * @date 15:13 2021/7/12
 */
@Slf4j
public class ValidateUtils {
    /**
     * 最大长度校验
     * 
     * @param text 校验目标
     * @param length 最大长度
     * @param msg 校验失败，返回异常msg
     */
    public static void lengthMax(String text, int length, String msg) {
        if (null != text && length < text.length()) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 最大长度校验
     * 
     * @param text 校验目标
     * @param length 最大长度
     * @param msg 校验失败，返回异常msg
     */
    public static void lengthMax(Integer text, int length, String msg) {
        if (null != text && length < String.valueOf(text).length()) {
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * 判断是否为数字
     * 
     * @param text 校验目标
     * @return Result 成功转化的值
     */
    public static Integer isInteger(String text, String msg) {
        if (StringUtils.hasLength(text)) {
            Integer num = null;
            try {
                num = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(msg);
            }
            return num;
        }
        return null;
    }

    /**
     * 判断是否为该特定值
     * 
     * @param text 校验目标
     * @return Result 成功转化的值
     */
    public static Long isLong(String text, String msg) {
        if (StringUtils.hasLength(text)) {
            Long num = null;
            try {
                num = Long.parseLong(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(msg);
            }
            return num;
        }
        return null;
    }

    /**
     * 校验：只能是限定的这些值
     * 
     * @param text 校验目标
     * @param limitValues 限定值
     * @param msg 不符合后的话术
     */
    public static void valueLimitEnum(String text, String[] limitValues, String msg) {
        if (null == limitValues || 0 == limitValues.length) {
            throw new IllegalArgumentException(msg + text);
        }
    }
}
