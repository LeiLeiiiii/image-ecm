package com.sunyard.framework.ocr.config;
/*
 * Project: SunAM
 *
 * File Created at 2025/9/30
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.mymonstercat.Model;
import io.github.mymonstercat.ocr.InferenceEngine;
import lombok.extern.slf4j.Slf4j;

/**
 * @Desc
 * @author Leo
 * @date 2025/9/30 10:37
 * @version
 */
@Slf4j
@Configuration
public class RapidOcrConfig {

    @Bean
    public InferenceEngine initEngine() {
        InferenceEngine engine = InferenceEngine.getInstance(Model.ONNX_PPOCR_V3);
        log.info("初始化RapidOcr成功!");
        return engine;
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/9/30 Leo creat
 */
