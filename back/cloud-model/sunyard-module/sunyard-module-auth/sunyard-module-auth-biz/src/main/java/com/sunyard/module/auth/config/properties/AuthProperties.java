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
@ConfigurationProperties("auth")
public class AuthProperties {
    /**
     * SessionId 过期时间
     */
    private Long sessionIdExpire = 1800L; // 默认3分钟
    /**
     * 初始化用户密码默认值
     */
    private String userInitPassword;
    /**
     * 是否进行密码过期检测的开关
     */
    private Boolean passwordCheck;
    /**
     * 过期天数
     */
    private Integer expireDay;

}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2024/7/17 Leo creat
 */
