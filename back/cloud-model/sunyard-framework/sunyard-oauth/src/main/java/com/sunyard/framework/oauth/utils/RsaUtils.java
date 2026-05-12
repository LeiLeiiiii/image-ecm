package com.sunyard.framework.oauth.utils;
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

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.utils
 * @Desc
 * @date 2021/7/1 16:05
 */
public class RsaUtils {
    /**
     * 非对称加密密钥算法
     */
    private static final String RSA = "RSA";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * 获取公钥
     *
     * @param publicKey 公钥字符串
     * @return Result
     */
    public static PublicKey getPublicKeyFromX509(String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        byte[] encodedKey = Base64.decodeBase64(publicKey.getBytes(StandardCharsets.UTF_8));
        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    }

    /**
     * 获取私钥
     *
     * @param privateKey 私钥字符串
     * @return Result
     */
    public static PrivateKey getPrivateKey(String privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        byte[] encodedKey = Base64.decodeBase64(privateKey.getBytes(StandardCharsets.UTF_8));
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    // ************************加密解密**************************
    /**
     * RSA加密
     *
     * @param data 待加密数据
     * @return Result
     */
    public static String encrypt(String data,String publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromX509(publicKey));
        int inputLen = data.getBytes(StandardCharsets.UTF_8).length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8), offset, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8), offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        // 获取加密内容使用base64进行编码,并以UTF-8为标准转化成字符串
        // 加密后的字符串
        return Base64.encodeBase64String(encryptedData);
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/1 zhouleibin creat
 */
