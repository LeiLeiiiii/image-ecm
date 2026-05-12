package com.sunyard.framework.message.config.properties;
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
 * @date 2024/7/17 15:48
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {
    private String password;
    private String username;
    private String isAuth;
    private String isSSL;
    private String port;
    private String smtpServer;
    private String fromAddress;
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2024/7/17 Leo creat
 */
