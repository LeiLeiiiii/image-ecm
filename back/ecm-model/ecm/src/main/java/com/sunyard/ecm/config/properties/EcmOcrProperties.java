package com.sunyard.ecm.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author yzy
 * @desc
 * @since 2025/11/19
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties("ocr")
public class EcmOcrProperties {

    @Value("${ocr.app-secret}")
    String appSecret;
    @Value("${ocr.app-key}")
    private String appKey;
    @Value("${ocr.ocr-host}")
    private String ocrHost;
    //默认1,1表示走sunyard的OCR识别归类,2走睿征
    @Value("${ocr.ocr-type:1}")
    private String ocrType;
    @Value("${ocr.auto-class-thread:0.8}")
    private String autoClassThread;
    @Value("${ocr.newUrl:http://172.1.1.211:9208/ocr}")
    private String getOcrUrl;
    /**
     * 单证识别类型 1：信雅达单证识别  2：瑞真单证识别
     */
    @Value("${ocr.ocr-document-type:1}")
    private String ocrDocumentType;

    @Value("${ocr.ocr-ignore-classId:4,5,7,8,10}")
    private String ocrIgnoreClassId;

    @Value("${ocr.ocr-split-length:30}")
    private String ocrSplitLength;
}
