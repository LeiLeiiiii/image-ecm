package com.sunyard.framework.common.util.encryption;
/*
 * Project: Sunyard
 *
 * File Created at 2023/6/29
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Leo
 * @Desc 生成appId和appSecret的工具类
 * @date 2023/6/29 10:41
 */
@Slf4j
public class AppUtils {
    /**
     * 生成 app_secret 密鑰
     */
    private final static String SERVER_NAME = "mazhq_abc123";
    private final static String[] CHARS = new String[] {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
        "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8",
        "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
        "V", "W", "X", "Y", "Z"};

    /**
     * @Description:
     *               <p>
     *               短8位UUID思想其實借鑑微博短域名的生成方式，可是其重複機率太高，並且每次生成4個，須要隨即選取一個。
     *               本算法利用62個可打印字符，經過隨機生成32位UUID，因爲UUID都爲十六進制，因此將UUID分紅8組，每4個爲一組，而後經過模62操做，結果做爲索引取出字符， 這樣重複率大大下降。
     *               經測試，在生成一千萬個數據也沒有出現重複，徹底知足大部分需求。
     *               </p>
     * @author mazhq
     * @date 2019/8/27 16:16
     */
    public static String getAppId() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 8; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(CHARS[x % 0x3E]);
        }
        return shortBuffer.toString();

    }

    /**
     * 經過appId和內置關鍵詞生成APP Secret
     * @param appId appId
     * @return
     */
    public static String getAppSecret(String appId) {
        try {
            String[] array = new String[] {appId, SERVER_NAME};
            StringBuffer sb = new StringBuffer();
            // 字符串排序
            Arrays.sort(array);
            for (int i = 0; i < array.length; i++) {
                sb.append(array[i]);
            }
            String str = sb.toString();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(str.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();

            StringBuffer hexstr = new StringBuffer();
            String shaHex = "";
            for (int i = 0; i < digest.length; i++) {
                shaHex = Integer.toHexString(digest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexstr.append(0);
                }
                hexstr.append(shaHex);
            }
            return hexstr.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("系统异常",e);
            throw new RuntimeException();
        }
    }
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/6/29 Leo creat
 */
