package com.sunyard.module.system.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author P-JWei
 * @date 2023/7/31 11:33:42
 * @title
 * @description
 */
@Data
public class SysApiAuthVO implements Serializable {
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
