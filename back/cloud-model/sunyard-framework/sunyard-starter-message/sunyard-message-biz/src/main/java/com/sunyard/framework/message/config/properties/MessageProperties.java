package com.sunyard.framework.message.config.properties;
/*
 * Project: Sunyard
 *
 * File Created at 2025/11/26
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author Leo
 * @Desc
 * @date 2025/11/26 8:18
 */
@Data
@RefreshScope
@Component
public class MessageProperties {

    @Value("${spring.application.name}")
    private String appName;
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/11/26 Leo creat
 */
