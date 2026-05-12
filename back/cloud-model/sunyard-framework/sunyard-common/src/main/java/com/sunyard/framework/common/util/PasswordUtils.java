/*
 * Project: smartzone-common
 *
 * File Created at 2018年9月18日
 *
 * Copyright 2016 CMCC Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of ZYHY Company. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */
package com.sunyard.framework.common.util;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhouleibin-bwf
 * @Type PasswordUitl.java
 * @Desc
 * @date 2018年9月18日 上午10:33:30
 */
public class PasswordUtils {

    /**
     * 所有字母 不包含 i,I,L,l,o,O
     */
    private static final String CHAR_REGEX = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
    /**
     * 所有数字 不包含 1 0
     */
    private static final String NUM_REGEX = "23456789";
    /**
     * 所用字母
     */
    private static final String FULL_CHAR_REGEX = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 所有数字
     */
    @SuppressWarnings("unused")
    private static final String FULL_NUM_REGEX = "0123456789";

    private static final String FULL_SPECIAL_CHARACTER_REGEX = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}";

    /**
     * 密码加密方法 迭代5次
     *
     * @param credentialsSalt 盐
     * @param credentials     密码
     * @return
     */
    public static String getEncryptionPassword(String credentialsSalt, String credentials) {
        //加密方式
        String hashAlgorithmName = "MD5";
        //加密次数
        int hashIterations = 1;
        //盐值
        ByteSource salt = ByteSource.Util.bytes(credentialsSalt);
        SimpleHash hash = new SimpleHash(hashAlgorithmName, credentials,salt, hashIterations);
        return hash.toString();
    }

    /**
     * 密码校验 密码长度为8~20位，至少包含两种不同类型字符（小写字母和数字）
     *
     * @param password 密码
     * @return Result
     */
    public static boolean passwordValidator(String password) {
        Integer hasUpperCaseChar = 0;
        Integer hasLowerCaseChar = 0;
        Integer hasSpecialChar = 0;
        Integer hasNum = 0;
        boolean length = false;
        String[] strArr = password.split("");
        for (int i = 0; i < strArr.length; i++) {
            String s = strArr[i];
            if (FULL_NUM_REGEX.contains(s)) {
                hasNum = 1;
            } else if (FULL_CHAR_REGEX.substring(0, FULL_CHAR_REGEX.length() >> 1).contains(s)) {
                hasLowerCaseChar = 1;
            } else if (FULL_CHAR_REGEX.substring(FULL_CHAR_REGEX.length() >> 1).contains(s)) {
                hasUpperCaseChar = 1;
            } else if (FULL_SPECIAL_CHARACTER_REGEX.contains(s)) {
                hasSpecialChar = 1;
            } else {
                return false;
            }
            if (i == strArr.length - 1) {
                return hasNum + hasLowerCaseChar + hasUpperCaseChar + hasSpecialChar >= 3;
            }
        }
        if (strArr.length >= 8 && strArr.length <= 20) {
            length = true;
        }
        return length;
    }

    /**
     * 弱密码校验 不可出现如下连续三位指定字符序列
     *
     * @param password 密码
     * @return Result
     */
    public static boolean weakPasswordValidator(String password) {
        String validateStr1 = "abcdefghijklmnopqrstuvwxyz\tzyxwvutsrqponmlkjihgfedcba\t0123456789\t987654321";
        String[] validateStr2 = new String[]{"qwertyuiop[]", "][poiuytrewq", "asdfghjkl;'", "';lkjhgfdsa",
                "zxcvbnm,./", "/.,mnbvcxz", "~!@#$%^&*()_+", "+_)(*&^%$#@!~"};
        for (int i = 0; i <= password.length() - 3; i++) {
            String s = password.toLowerCase().substring(i, i + 3);
            if (validateStr1.contains(s)) {
                return false;
            } else {
                for (String ms : validateStr2) {
                    if (ms.contains(s)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 产生指定位数随机密码，包含字母和数字
     *
     * @param length > 1
     * @return Result
     */
    public static String generatePassword(int length) {
        int numCount = 0;
        int charCount = 0;
        while (charCount <= 0) {
            numCount = new SecureRandom().nextInt(length + 1);
            charCount = length - numCount;
        }
        List<String> list = new ArrayList<String>();
        list = getRandomPassList(NUM_REGEX, numCount, list);
        list = getRandomPassList(CHAR_REGEX, charCount, list);
        Collections.shuffle(list);
        String res = String.join("", list);
        if (!passwordValidator(res) || !weakPasswordValidator(res)) {
            res = generatePassword(length);
        }
        return res;
    }

    /**
     * 根据数字和字母模板产生指定数量的数字和字母放入密码元素列表
     *
     * @param regex       模板
     * @param randomCount 数量
     * @param passList    密码集
     * @return Result
     */
    private static List<String> getRandomPassList(String regex, int randomCount, List<String> passList) {
        for (int i = 0; i < randomCount; i++) {
            int index = (int) (Math.random() * regex.length());
            passList.add(String.valueOf(regex.charAt(index)));
        }
        return passList;
    }
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2018年9月18日 zhouleibin-bwf
 * creat
 */
