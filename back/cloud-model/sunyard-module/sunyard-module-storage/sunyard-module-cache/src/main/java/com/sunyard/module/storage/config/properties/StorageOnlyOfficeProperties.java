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
public class StorageOnlyOfficeProperties {

    @Value("${onlyOffice.enable:false}")
    private Boolean useOnlyOffice;
    @Value("${onlyOffice.maxFileSize:104857600}")
    private Long maxFileSize;
    @Value("${onlyOffice.storageUrl:http://127.0.0.1:28083}")
    private String storageUrl;
    @Value("${onlyOffice.mapping:/storage/deal/getFileByFileIdByWater?}")
    private String storageMapping;
    @Value("${onlyOffice.checkFileUrl:http://172.1.1.210:28083/storage/deal/getFileByFileIdByWater?}")
    private String checkFileUrl;
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/9/21 Leo creat
 */
