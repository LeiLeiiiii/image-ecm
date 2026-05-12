package com.sunyard.module.system.config.properties;
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
@ConfigurationProperties(prefix = "system")
public class SystemProperties {
    /**
     * 初始化用户密码默认值
     */
    private String userInitPassword;
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2024/7/17 Leo creat
 */
