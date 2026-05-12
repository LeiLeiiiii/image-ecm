package com.sunyard.framework.message.config;
/*
 * Project: Sunyard
 *
 * File Created at 2023/5/31
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Leo
 * @date 2023/5/31 11:48
 */
@Configuration
@ComponentScan(basePackages = "com.sunyard.framework.message")
public class MessageAutoConfiguration {
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/31 Leo creat
 * 2025/11/26 Leo update 自动配置增强
 */
