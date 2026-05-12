package com.sunyard.module.auth.config.properties;
/*
 * Project: Sunyard
 *
 * File Created at 2025/9/21
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author Leo
 * @Desc
 * @date 2025/9/21 19:52
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties("auth.oauth2")
public class AuthOauth2Properties {

    /**
     * oauth2 密码模式clientId
     */
    private String clientId;
    /**
     * oauth2 密码模式clientSecret
     */
    private String clientSecret;

    /**
     * api token 过期时间
     */
    private String apiTokenTimeOut = "30000";

    @Value("${server.port:28081}")
    private String port;
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/9/21 Leo creat
 */
