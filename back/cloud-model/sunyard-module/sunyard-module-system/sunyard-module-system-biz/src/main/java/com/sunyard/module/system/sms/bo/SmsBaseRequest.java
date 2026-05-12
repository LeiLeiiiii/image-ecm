package com.sunyard.module.system.sms.bo;
/*
 * Project: SunAM
 *
 * File Created at 2025/8/15
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @Desc
 * @author Leo
 * @date 2025/8/15 9:59
 * @version
 */
@Slf4j
@Data
public abstract class SmsBaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 基础的url信息 */
    private String baseUrl;
    /** 商家的appSecret  */
    protected String appSecret;
    /** 商家的appKey  */
    protected String appKey;
    /** 时间戳（YYYYMMDDHHMMSS） */
    protected String timestamp;
    /** 明文加密后的字符串 */
    protected String sign;

    private static final String PARAMETER_PREFIX = "get";

    public String getSign() {
        String[] arr = new String[] {
                "appKey=" + this.getAppKey(),
                "timestamp=" + this.getTimestamp(),
                "data=" + this.toPostRequest()};
        // appKey、timestamp、data 参数进行字典序排序
        Arrays.sort(arr);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            content.append(arr[i]);
            if (i != arr.length - 1) {
                content.append("&");
            }
        }

        String result = content.toString();
        log.info("签名参数：{}", result);
        result = Base64.encode(result);
        log.info("签名参数Base64：{}", result);
        result = appSecret + ":" + result;
        log.info("result：{}", result);
        String signature = SecureUtil.sha1(result);
        log.info("----------- 生成sign：{}", signature);
        return signature;
    }

    public abstract String getUrl();

    public String toPostRequest() {
        JSONObject jsonObject = new JSONObject();

        Method[] methods = getClass().getMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method method : methods) {
            if (method.getName().startsWith(PARAMETER_PREFIX) && !method.getName().endsWith("Url")
                    && !method.getName().equals("getAppSecret")
                    && !method.getName().equals("getAppKey")
                    && !method.getName().equals("getTimestamp")
                    && !method.getName().equals("getHttpEntity")
                    && !method.getName().equals("getClass")
                    && !method.getName().equals("getSign")) {
                String fieldName = method.getName().substring(3);
                fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                try {
                    Object value = method.invoke(this);
                    if (value != null) {
                        jsonObject.put(fieldName, value);
                    }
                } catch (Exception e) {
                    log.error("获取get方法数据错误", e);
                    throw new RuntimeException("获取get方法数据错误", e);
                }
            }
        }
        return jsonObject.toString();
    }

    public HttpEntity<String> getHttpEntity(HttpHeaders headers) {
        return new HttpEntity<String>(toPostRequest(), headers);
    }

}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/8/15 Leo creat
 */
