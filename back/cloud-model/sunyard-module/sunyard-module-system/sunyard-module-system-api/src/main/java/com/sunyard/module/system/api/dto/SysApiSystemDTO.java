package com.sunyard.module.system.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @author P-JWei
 * @date 2024/4/10 16:13:00
 * @title
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysApiSystemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

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
     * appId
     */
    private String appId;

    /**
     * appSecret
     */
    private String appSecret;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 加签方式
     */
    private String signType;

    /**
     * 数据格式
     */
    private String format;

    /**
     * 编码格式
     */
    private String charset;

    /**
     * 到期时间
     */
    private Date expirationTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1)
     */
    private Integer isDeleted;
}
