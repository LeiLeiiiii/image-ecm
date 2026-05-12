package com.sunyard.ecm.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * @author yzy
 */
@Data
public class EcmDownloadBatchDTO {

    @JacksonXmlProperty(localName = "APP_CODE")
    private String appCode;

    @JacksonXmlProperty(localName = "APP_NAME")
    private String appName;

    @JacksonXmlProperty(localName = "BUSI_NO")
    private String busiNo;

}
