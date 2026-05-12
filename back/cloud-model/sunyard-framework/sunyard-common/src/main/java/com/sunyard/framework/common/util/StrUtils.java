package com.sunyard.framework.common.util;
/*
 * Project: am
 *
 * File Created at 2021/7/13
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.util.ObjectUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhouleibin
 * @Type
 * @Desc
 * @date 2021/7/13 17:23
 */
public class StrUtils {
    private static final Pattern LINE_PATTERN = Pattern.compile("_(\\w)");
    private static final Pattern HUMP_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern BUMBER_PATTERN = Pattern.compile("[0-9]*");

    /**
     * 将字符串的首字母转大写
     *
     * @param str 需要转换的字符串
     * @return Result
     */
    public static String captureName(String str) {
        // 进行字母的ascii编码前移，效率要高于截取字符串进行转换的操作
        char[] cs = str.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
    }

    /**
     * 返回字符串、null转“”
     *
     * @param str 字符串
     * @return Result
     */
    public static String toEmpty(String str) {
        return str == null ? "" : str;
    }

    /**
     * 下划线转驼峰
     * @param str 字符串
     * @return Result
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = LINE_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 驼峰转下划线(简单写法，效率低于{@link #humpToLine2(String)})
     * @param str 字符串
     * @return String
     */
    public static String humpToLine(String str) {
        return str.replaceAll("[A-Z]", "_$0").toLowerCase();
    }

    /**
     * 驼峰转下划线,效率比上面高
     * @param str 字符串
     * @return String
     */
    public static String humpToLine2(String str) {
        Matcher matcher = HUMP_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 判断是否含有特殊字符
     * @param name 姓名
     * @param str 字符串
     */
    public static void isSpecialChar(String name, String str) {
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        // 判断是否含有特殊字符
        if (m.find()) {
            throw new IllegalArgumentException(name + "不能含有特殊字符");
        }
    }

    /**
     * 判断是否全为数字
     *
     * @param str 字符串
     * @return Result
     */
    public static boolean isNumeric(String str) {
        return BUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 字符串+1方法，该方法将其结尾的整数+1,适用于任何以整数结尾的字符串,不限格式，不限分隔符。
     *
     * @param testStr 要+1的字符串
     * @return Result +1后的字符串
     * @throws NumberFormatException 异常
     * @author zwl
     */
    public static String addOne(String testStr) {
        String[] strs;
        if (testStr.length() >= 5) {
            // 根据不是数字的字符拆分字符串
            strs = testStr.substring(testStr.length() - 5).split("[^0-9]");
        } else {
            strs = testStr.split("[^0-9]");
        }
        if (!ObjectUtils.isEmpty(strs)) {
            // 取出最后一组数字
            String numStr = strs[strs.length - 1];
            // 如果最后一组没有数字(也就是不以数字结尾)，抛NumberFormatException异常
            if (numStr != null && numStr.length() > 0) {
                // 取出字符串的长度
                int n = numStr.length();
                // 将该数字加一
                int num = Integer.parseInt(numStr) + 1;
                String added = String.valueOf(num);
                n = Math.min(n, added.length());
                // 拼接字符串
                return testStr.subSequence(0, testStr.length() - n) + added;
            } else {
                return getRandomString(5);
            }
        } else {
            return getRandomString(5);
        }
    }

    /**
     * 用户根据length产生字符串的长度
     *
     * @param length 长度
     * @return Result
     */
    public static String getRandomString(Integer length) {
        String str = "0";
        StringBuffer sb = new StringBuffer();
        for (Integer i = 0; i < length; i++) {
            sb.append(str);
        }
        return StrUtils.addOne(sb.toString());
    }

    /**
     * 驼峰式命名法
     * @param s 字符串
     * @return
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String s1 = s.charAt(i) + "";

            if (("_").equals(s1)) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(s1);
            }
        }
        return sb.toString();
    }

}
