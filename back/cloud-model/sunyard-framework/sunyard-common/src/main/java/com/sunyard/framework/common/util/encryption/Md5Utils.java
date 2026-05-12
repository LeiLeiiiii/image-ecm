package com.sunyard.framework.common.util.encryption;
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
import org.apache.commons.codec.digest.DigestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.utils
 * @Desc
 * @date 2021/7/1 15:22
 */
@Slf4j
public class Md5Utils {

    private static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * 生成32位md5
     *
     * @param str 需加密string
     * @return Result
     */
    public static String md5Hex(String str) {
        return DigestUtils.md5Hex(str);
    }

    /**
     * 获取文件的md5
     * @param inputStream 输入流
     * @return Result
     * @throws NoSuchAlgorithmException 异常
     * @throws IOException 异常
     */
    public static String calculateMD5(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        DigestInputStream dis = new DigestInputStream(inputStream, md);

        // 读取输入流中的数据，触发 MD5 计算
        byte[] buffer = new byte[4096];
        while (dis.read(buffer) != -1) {
            // 读取输入流的数据，不做任何处理
        }

        // 如果是 ByteArrayInputStream，直接重置
        if (inputStream instanceof ByteArrayInputStream) {
            inputStream.reset();
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
     * 获取文件md5
     * @param bytes byte数组
     * @return Result
     * @throws NoSuchAlgorithmException 异常
     * @throws IOException 异常
     */
    public static String calculateMD5(byte[] bytes){
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
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
     * 获取文件MD5
     * @param fileName 文件名
     * @param hashType  算法
     * @return Result java.lang.String
     * @author lei2.chen
     * @date 2022.2.13 17:29
     */
    public static String getHash(String fileName, String hashType) {
        boolean flag = true;
        File f = new File(fileName);
        MessageDigest md5 = null;
        InputStream ins = null;

        try {

            ins = new FileInputStream(f);

            byte[] buffer = new byte[8192];

            md5 = MessageDigest.getInstance(hashType);
            int len;
            while ((len = ins.read(buffer)) != -1) {
                md5.update(buffer, 0, len);
            }

        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("异常描述",e);
            throw new RuntimeException(e);
        } finally {
            try {
                if(ins!=null){
                    ins.close();
                }
            } catch (IOException e) {
                log.error("异常描述",e);
                throw new RuntimeException(e);
            }

        }
        if(flag && md5!=null){
            return toHexString(md5.digest());
        }else{
            return "";
        }
    }

    /**
     *  转为16进制
     * @param b byte数组
     * @return Result java.lang.String
     * @author lei2.chen
     * @date 2022.2.13 17:30
     */
    protected static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
            sb.append(hexChar[b[i] & 0x0f]);
        }
        return sb.toString();
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/1 zhouleibin creat
 */
