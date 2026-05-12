package com.sunyard.module.auth.config.properties;
/*
 * Project: Sunyard
 *
 * File Created at 2024/7/17
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author Leo
 * @Desc
 * @date 2024/7/17 16:16
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties("auth.verify-code")
public class AuthVerifyCodeProperties {
    /**
     * 登录校验码开关，用于测试及开发便捷
     */
    private Boolean enable;

    /**
     * 验证码类型
     */
    private String type;

}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2024/7/17 Leo creat
 */
