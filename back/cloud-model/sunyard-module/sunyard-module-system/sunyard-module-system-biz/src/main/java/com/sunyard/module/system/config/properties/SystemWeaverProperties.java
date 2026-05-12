package com.sunyard.module.system.config.properties;
/*
 * Project: Sunyard
 *
 * File Created at 2025/9/21
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author Leo
 * @Desc
 * @date 2025/9/21 19:11
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "system.weaver")
public class SystemWeaverProperties {

    /**
     * 接口前缀url
     */
    private String urlPrefix = "http://127.0.0.1";

}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/9/21 Leo creat
 */
