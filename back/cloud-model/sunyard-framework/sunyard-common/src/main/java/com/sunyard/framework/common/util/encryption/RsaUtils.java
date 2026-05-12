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

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

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
    public static final String PUBLIC_KEY =
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDibHXsVcLC1JsVePvx+3iHFIITlggfDPIznhJD"
            + "klN8CQ8Pcq76kSId0qsn6x2ysv1koRP2qjKKbScuEDuK9HrBL/HjYa/8h7mbuPjr5pDfE3nfNG9v"
            + "LXeloorfj/AM4xWbhpvt7NwIfVyYP3SDxR35OXHrtP4bqDM7dilKIfS1MwIDAQAB";
    private static final String PRIVATE_KEY =
        "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAOJsdexVwsLUmxV4+/H7eIcUghOW"
            + "CB8M8jOeEkOSU3wJDw9yrvqRIh3SqyfrHbKy/WShE/aqMoptJy4QO4r0esEv8eNhr/yHuZu4+Ovm"
            + "kN8Ted80b28td6Wiit+P8AzjFZuGm+3s3Ah9XJg/dIPFHfk5ceu0/huoMzt2KUoh9LUzAgMBAAEC"
            + "gYA45vUDvdHCUNfgn5UBjScPG3JNJclItGIx4qnIXX+pjkVAppue0NZ4FDd7QrUl8aGIpopz1PX6"
            + "n8/W3bF5DFPEvBL3FJkiCgIJZZCeVYtg6AqvvQb+MWQe0da3FNx9kgY8BWY8A3S1dSbzI8w8VNvr"
            + "Qnd2VzUpJQ7sJRZEr4Tp8QJBAPZSNxATsO1/EZ3g2hJnM5pecJRYz0YCuhGBOS9Byis8aKGhpl0L"
            + "xVB+W8wuscJa105BYv8vGOcDg4mFeHqFEj8CQQDrUhfyuu/g6z8Jj5GnvoGrmTZ8q0s/rR8z0Mce"
            + "YRC4+Hiwp5EOS2oJNIdTqsnRZHilG9FRj7Zm6MJ2cP7bizgNAkEAoTDmFaA9LP31glJtgpOEgmWA"
            + "2KNRaKhKKUBeMp2j9i0+717AZq1YPzehTPnVm7EkqnJBnWqtqidzgaAVWmAQswJBAOFZdGWWbzFN"
            + "FsjpG+svnK3fwzYQM7d+6mqMfKKzAXihObKyRU8TGTBHhXCyFSLYvFAhG4qnvV0/eTpDa0yDPeEC"
            + "QFbjq41NsxwPW6mDnA2KyL2toAT6IYF8zgDenzROjUVF856ad6W5terrT851alnZPEm9gXu6Ko6f" + "6eGSLOb52HM=";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;


    /**
     * 生成RSA公、私钥对
     *
     * @return Result
     */
    public static Map<String, Object> generateRsaKeyPairs() throws NoSuchAlgorithmException {
        Map<String, Object> keyPairMap = new HashMap<String, Object>(6);
        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
        KeyPair keyPair = generator.genKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        keyPairMap.put("publicKey", Base64.encodeBase64String(publicKey.getEncoded()));
        keyPairMap.put("privateKey", Base64.encodeBase64String(privateKey.getEncoded()));
        return keyPairMap;
    }

//    /**
//     * 获取密钥对
//     *
//     * @return Result 密钥对
//     */
//    public static KeyPair getKeyPair() throws Exception {
//        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
//        generator.initialize(2048);
//        return generator.generateKeyPair();
//    }

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

    // ************************加密解密**************************
    /**
     * RSA加密
     *
     * @param data 待加密数据
     * @return Result
     */
    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromX509(PUBLIC_KEY));
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

    /**
     * RSA解密
     *
     * @param data 待解密数据
     * @return Result
     */
    public static String decrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(PRIVATE_KEY));
        byte[] dataBytes = Base64.decodeBase64(data);
        int inputLen = dataBytes.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offset = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offset > 0) {
            if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
            }
            out.write(cache, 0, cache.length);
            i++;
            offset = i * MAX_DECRYPT_BLOCK;
        }
        out.close();
        // 解密后的内容
        return out.toString("UTF-8");
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/1 zhouleibin creat
 */
