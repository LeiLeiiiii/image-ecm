package com.sunyard.framework.rpc.config;
/*
 * Project: Sunyard
 *
 * File Created at 2023/4/21
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author Leo
 * @Desc 出来处理服务在nacos中注册后订阅者应用名显示未unknow的问题
 */
@Configuration(proxyBeanMethods = false)
public class ProjectNameConfig implements EnvironmentAware {

    private static final String PROJECT_NAME = "project.name";
    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public void setEnvironment(Environment environment) {
        if (!StringUtils.hasText(System.getProperty(PROJECT_NAME))) {
            System.setProperty(PROJECT_NAME, applicationName);
        }
    }
}
