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
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Leo
 * @Desc
 * @date 2023/6/29 10:42
 */
@Slf4j
public class SignatureUtils {

    // ***************************签名和验证*******************************

    /**
     * 私钥签名
     * @param content 内容
     * @param privateKey 私钥
     * @return 签名
     */
    public static String sign(String content, String privateKey) {
        try {
            PrivateKey priKey = RsaUtils.getPrivateKey(privateKey);
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initSign(priKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            byte[] signed = signature.sign();
            return new String(Base64.encodeBase64(signed),StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 公钥验签
     * @param content 内容
     * @param sign 签名
     * @param publicKey 公钥
     * @return 是否验签通过
     */
    public static boolean rsaCheckContent(String content, String sign, String publicKey) {
        try {
            PublicKey pubKey = RsaUtils.getPublicKeyFromX509(publicKey);
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initVerify(pubKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.decodeBase64(sign.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 验签
     * @param params  参数
     * @param publicKey 公钥
     * @return 是否验签通过
     */
    public static boolean rsaCheck(Map<String, String> params, String publicKey) {
        String sign = params.get("sign");
        String content = getSignCheckContentV1(params);
        try {
            return rsaCheckContent(content, sign, publicKey);
        } catch (Exception e) {
            throw new RuntimeException("验签异常!");
        }
    }

    /**
     * 获取签名
     * @param params 参数
     * @return 签名
     */
    public static String getSignCheckContentV1(Map<String, String> params) {
        if (params == null) {
            return null;
        }

        params.remove("sign");
        params.remove("sign_type");

        StringBuffer content = new StringBuffer();
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            content.append((i == 0 ? "" : "&") + key + "=" + value);
        }

        return content.toString();
    }

}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/6/29 Leo creat
 */
