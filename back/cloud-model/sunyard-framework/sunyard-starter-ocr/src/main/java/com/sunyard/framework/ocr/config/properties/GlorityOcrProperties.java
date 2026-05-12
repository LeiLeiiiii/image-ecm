package com.sunyard.framework.ocr.config.properties;
/*
 * Project: Sunyard
 *
 * File Created at 2024/7/17
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author Leo
 * @Desc
 * @date 2024/7/17 14:14
 */
@Data
@Component
@ConfigurationProperties(prefix = "ocr")
public class GlorityOcrProperties {
    private String appKey;
    private String appSecret;
    /**
     * 调验真接口的地址
     */
    private String validateHost;
    /**
     * 调ocr接口的地址
     */

    private String ocrHost;
    /**
     * 指定Ocr操作者名称(对应OCROperator实现类@Service注解的value值)
     */
    private String operatorName;
    /**
     * 超时时间,等待校验任务完成的最长时间,超过此时间将不再等待线程池回复,直接报错
     */
    private String threadWaitTimeOut;
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2024/7/17 Leo creat
 */
