package com.sunyard.framework.common.util.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * @author P-JWei
 * @date 2023/12/4 10:02:07
 * @title
 * @description
 */
public class AesUtils {

    /**
     * 非对称加密密钥算法
     */
    private static final String AES = "AES";

    /**
     * 加密类型
     */
    private static final String AES_TYPE = "AES/ECB/PKCS5Padding";

    /**
     * 加密
     *
     * @param text 加密内容
     * @param key key
     * @return Result
     * @throws Exception 异常
     */
    public static String encrypt(byte[] text, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 创建AES加密算法实例(根据传入指定的秘钥进行加密)
        Cipher cipher = Cipher.getInstance(AES_TYPE);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES);
        // 初始化为加密模式，并将密钥注入到算法中
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        // 将传入的文本加密
        byte[] encrypted = cipher.doFinal(text);
        //生成密文
        // 将密文进行Base64编码，方便传输
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 解密
     *
     * @param base64Encrypted 加密后的内容
     * @param key key
     * @return Result
     * @throws Exception 异常
     */
    public static byte[] decrypt(byte[] base64Encrypted, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 创建AES解密算法实例
        Cipher cipher = Cipher.getInstance(AES_TYPE);
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES);
        // 初始化为解密模式，并将密钥注入到算法中
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        // 将Base64编码的密文解码
        byte[] encrypted = Base64.getDecoder().decode(base64Encrypted);
        // 解密
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    /**
     * 生成16位私钥
     *
     * @return Result
     */
    private static String getKey() {
        UUID id = UUID.randomUUID();
        String[] idd = id.toString().split("-");
        return idd[0] + idd[1] + idd[2];
    }
}
