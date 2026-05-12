package com.sunyard.framework.redis.config;
/*
 * Project: Sunyard
 *
 * File Created at 2023/5/24
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/24 13:08
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheAutoConfiguration {}