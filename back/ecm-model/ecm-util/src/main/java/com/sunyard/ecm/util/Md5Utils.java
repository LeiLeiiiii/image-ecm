package com.sunyard.ecm.util;
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


import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author: lw
 * @Date: 2024/1/28
 * @Description: 文件md5转换工具类
 */
@Slf4j
public class Md5Utils {
    /**
     *
     * @param bytes
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String calculateMD5(byte[] bytes){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("获取md5异常",e);
        }
        // 获取计算得到的 MD5 值
        byte[] md5Bytes = md.digest(bytes);

        // 将字节数组转换为十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Bytes) {
            sb.append(String.format("%02x", b));
        }

        // 返回 MD5 值的十六进制字符串表示
        return sb.toString();
    }

    /**
     *
     * @param inputStream
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public static String calculateMD5(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        DigestInputStream dis = new DigestInputStream(inputStream, md);

        // 读取输入流中的数据，触发 MD5 计算
        byte[] buffer = new byte[4096];
        while (dis.read(buffer) != -1) {
            // 读取输入流的数据，不做任何处理
        }

        // 获取计算得到的 MD5 值
        byte[] md5Bytes = md.digest();

        // 将字节数组转换为十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Bytes) {
            sb.append(String.format("%02x", b));
        }

        // 返回 MD5 值的十六进制字符串表示
        return sb.toString();
    }

    public static String encryptHmacMd5Str(String key, String inStr) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        if (inStr == null || "".equals(inStr)) {
            throw new IllegalArgumentException("Parameter[inStr] can't be null.");
        }
        if (key == null || "".equals(key)) {
            throw new IllegalArgumentException("Parameter[key] can't be null.");
        }

//    	char[] charArray = inStr.toCharArray();
//		byte[] byteArray = new byte[charArray.length];
//		for (int i = 0; i < charArray.length; i++) {
//			byteArray[i] = (byte) charArray[i];
//		}

        byte[] byteArray = inStr.getBytes("UTF-8");

        SecretKeySpec sk = new SecretKeySpec(key.getBytes(), "HmacMD5");
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(sk);

        byte[] md5Bytes = mac.doFinal(byteArray) ;
        return byte2Hex(md5Bytes);
    }

    public static String byte2Hex(byte[] bytes) {
        StringBuilder hexSb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int val = bytes[i] & 0XFF;
            if (val < 16) {
                hexSb.append('0');
            }
            hexSb.append(Integer.toHexString(val));
        }
        return hexSb.toString();
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/1 zhouleibin creat
 */
