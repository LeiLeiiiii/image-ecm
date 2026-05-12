package com.sunyard.framework.mybatis.config;
/*
 * Project: Sunyard
 *
 * File Created at 2023/6/14
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Leo
 * @Desc
 * @date 2023/6/14 16:12
 */
@Configuration
public class JacksonAutoConfiguration {
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 统计处理long、date类型
     * @return result
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // Long 会自定转换成 String,分布式id 超过前端js解析的长度，会丢失精度，转换为string
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            // 统一时间格式返回
            builder.serializerByType(Date.class, new DateSerializer(false, new SimpleDateFormat(DATETIME_FORMAT)));
        };
    }
}