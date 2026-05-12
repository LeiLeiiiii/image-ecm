package com.sunyard.module.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author P-JWei
 * @date 2023/7/31 11:37:57
 * @title
 * @description
 */
@Data
public class SysApiAuthDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 主键
     */
    private Long id;

    /**
     * appId
     */
    private String appId;

    /**
     * appSecret
     */
    private String appSecret;

    /**
     * 接入系统名
     */
    private String systemName;

    /**
     * 接入系统Referer
     */
    private String systemReferer;

    /**
     * 授权状态（0已授权 1未授权）
     */
    private Integer status;

    /**
     * 授权状态
     */
    private String statusStr;

    /**
     * 数据格式
     */
    private String format;

    /**
     * 编码格式
     */
    private String charset;

    /**
     * 加签方式
     */
    private String signType;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 私钥
     */
    private String privateKey;
}
