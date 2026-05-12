package com.sunyard.framework.log.config;
/*
 * Project: com.sunyard.am.config
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouleibin
 */
@Configuration
@ComponentScan(basePackages = "com.sunyard.framework.log.aop")
public class LogAutoConfiguration {

}
