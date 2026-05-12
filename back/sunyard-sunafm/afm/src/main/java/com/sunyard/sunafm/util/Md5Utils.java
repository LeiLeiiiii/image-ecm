package com.sunyard.sunafm.util;
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

import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.utils
 * @Desc
 * @date 2021/7/1 15:22
 */
//todo 底座和hutool都有
public class Md5Utils {

    public static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return ""; // 没有找到'.'，返回空字符串
        }
        return filename.substring(dotIndex + 1);
    }
    /**
     * 将文件转为base64
     * @return
     * @throws Exception
     */
    public static String convertMultipartFileToBase64(byte[] fileContent){
        // 读取MultipartFile内容为字节数组
        // 使用Base64编码
        String encodedString = Base64.getEncoder().encodeToString(fileContent);

        return encodedString;
    }

    /**
     * 生成32位md5
     *
     * @param str
     * @return
     */
    public static String md5Hex(String str) {
        return DigestUtils.md5Hex(str);
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
            e.printStackTrace();
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
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/1 zhouleibin creat
 */
