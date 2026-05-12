package com.sunyard.module.auth.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author P-JWei
 * @date 2023/10/13 9:00:36
 * @title
 * @description
 */
@Data
public class OpenAuthDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * token
     */
    private String token;

    /**
     * appid
     */
    private String appid;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 签名
     */
    private String sign;

    /**
     * referer
     */
    private String referer;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 参数
     */
    private String param;

    /**
     * 存储签名验证所需的待签名数据（适配多种类型）
     */
    private Object signData;

    /**
     * 请求头签名
     */
    private String requestSign;

    private String contentType;
}
