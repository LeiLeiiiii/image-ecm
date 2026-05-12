package com.sunyard.framework.ocr.config.properties;
/*
 * Project: Sunyard
 *
 * File Created at 2025/10/10
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
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author Leo
 * @Desc
 * @date 2025/10/10 19:43
 */
@Data
@Component
public class OcrProperties {

    @Value("${ocr.rapid-ocr.file-temp-dir:/home/temp/rapidOcr}")
    private String rapidOcrFileTempDir;
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/10/10 Leo creat
 */
