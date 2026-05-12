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
public class StorageNonBankEcmProperties {
    @Value("${sunecm.port:8080}")
    private String port;
    @Value("${sunecm.port2:8096}")
    private String port2;
    @Value("${sunecm.ip:127.0.0.1}")
    private String ip;
    @Value("${sunecm.ip2:127.0.0.1}")
    private String ip2;
    @Value("${sunecm.referer:SunAM}")
    private String referer;
    @Value("${sunecm.server-parameter:SunICMS}")
    private String serverParameter;
    @Value("${sunecm.organ-code:000000}")
    private String organCode;
    @Value("${sunecm.key:KP0avAHqX0QDqXpKEvTJVA1e}")
    private String key;
    @Value("${sunecm.interface-address:servlet/RouterServlet}")
    private String interfaceAddress;
    @Value("${sunecm.doc-code:DAZL}")
    private String docCode;
    @Value("${sunecm.doc-name:档案资料}")
    private String docName;
    @Value("${sunecm.role:role1}")
    private String role;
    @Value("${sunecm.destroy-identify-app-code:ArcDestroyIdy}")
    private String destroyIdentifyAppCode;

    @Value("${sunecm.socketPort:22203}")
    private Integer socketPort;
    @Value("${sunecm.server-name:SunAM}")
    private String systemName;
    @Value("${sunecm.com-code:00000002}")
    private String comCode;
    @Value("${sunecm.location:/home/temp}")
    private String location;

}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/9/21 Leo creat
 */
