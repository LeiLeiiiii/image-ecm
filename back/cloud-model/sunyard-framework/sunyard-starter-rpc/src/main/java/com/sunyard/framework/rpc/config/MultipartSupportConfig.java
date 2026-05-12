package com.sunyard.framework.rpc.config;
/*
 * Project: Sunyard
 *
 * File Created at 2023/5/18
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

/**
 * @author Leo
 * @Desc fegin 之间文件传输使用，现暂未启用
 * @date 2023/5/18 11:34
 */
public class MultipartSupportConfig {
    @Resource
    private ObjectFactory<HttpMessageConverters> messageConverters;

    /**
     *
     * @return
     */
    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/18 Leo creat
 */