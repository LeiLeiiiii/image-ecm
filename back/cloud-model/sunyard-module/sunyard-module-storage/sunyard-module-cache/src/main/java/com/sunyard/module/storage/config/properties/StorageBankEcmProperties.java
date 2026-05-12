package com.sunyard.module.storage.config.properties;
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author Leo
 * @Desc
 * @date 2025/9/21 20:26
 */
@Data
@RefreshScope
@Component
public class StorageBankEcmProperties {
    @Value("${backEcm.ip:127.0.0.1}")
    private String ecmIp;
    @Value("${backEcm.port:8023}")
    private Integer ecmPort;
    @Value("${backEcm.userNo:admin}")
    private String userNo;
    @Value("${backEcm.password:111}")
    private String password;
    @Value("${backEcm.groupName:ECMGROUP}")
    private String groupName;
    @Value("${backEcm.modelCode:CS}")
    private String modelCode;
    @Value("${backEcm.filePart:CS_PART}")
    private String filePart;
    @Value("${backEcm.indexName:CREATEDATE}")
    private String indexName;
    @Value("${backEcm.prefixPath:}")
    private String prefixPath;

}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/9/21 Leo creat
 */
