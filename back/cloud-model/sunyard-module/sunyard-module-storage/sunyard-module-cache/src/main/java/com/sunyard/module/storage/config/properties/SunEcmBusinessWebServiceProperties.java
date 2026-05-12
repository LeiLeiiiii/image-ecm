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

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author Leo
 * @Desc
 * @date 2025/9/21 20:26
 */
@Data
@RefreshScope
@Component
public class SunEcmBusinessWebServiceProperties {
    @Value("${sunecm.appCodeWebServiceUrl:http://172.1.1.210:8096/SunICMS/webServices/businessWebService?wsdl}")
    private String wsdlUrl;
    @Value("${sunecm.enable:false}")
    private Boolean enable;

}

