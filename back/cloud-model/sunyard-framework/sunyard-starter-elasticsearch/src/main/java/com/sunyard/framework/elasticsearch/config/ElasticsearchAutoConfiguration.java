package com.sunyard.framework.elasticsearch.config;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */


import org.dromara.easyes.starter.register.EsMapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouleibin
 * @date 2021/11/5 14:26
 * @Desc
 */
@Configuration
@ComponentScan(basePackages = "com.sunyard.framework.elasticsearch")
@EsMapperScan("com.sunyard.**.es")
public class ElasticsearchAutoConfiguration {}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2021/11/5 zhouleibin creat
 */
