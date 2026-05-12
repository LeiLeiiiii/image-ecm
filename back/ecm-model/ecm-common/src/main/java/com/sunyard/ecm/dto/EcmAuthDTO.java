package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author： lw
 * @create： 2023/12/28 14:27
 * @desc: 接入系统验签参数
 */
@Data
public class EcmAuthDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 接入系统ID
     */
    private String appId;

    /**
     * 接入系统名称
     */
    private String appName;

    /**
     * 接入系统referer
     */
    private String referer;

    /**
     * 当前时间戳
     */
    private String currentTime;

    /**
     * 签名（业务类型+业务编号）
     */
    private String sign;


}
