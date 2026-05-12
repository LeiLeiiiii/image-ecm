package com.sunyard.framework.ocr.dto.ocr;

import lombok.Data;

/**
 * ocr并验真 入参
 * @author PJW
 */
@Data
public class OCRRequestParamDTO {
    /**
     * 文件http地址
     */
    private String imageUrl;
    /**
     * 文件base64
     */
    private String imageData;
    /**
     * 睿真 app-key
     */
    private String appKey;
    /**
     * 睿真 ocr接口的地址
     */
    private String ocrHost;
    /**
     * 睿真 调验真接口的地址
     */
    private String validateHost;
    /**
     * 睿真 app-secret
     */
    private String appSecret;
}
