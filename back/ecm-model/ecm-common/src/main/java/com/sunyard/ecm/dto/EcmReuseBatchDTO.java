package com.sunyard.ecm.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * @author yzy
 */
@Data
public class EcmReuseBatchDTO {

    @JacksonXmlProperty(localName = "APP_CODE")
    private String appCode;

    @JacksonXmlProperty(localName = "APP_NAME")
    private String appName;

    @JacksonXmlProperty(localName = "BUSI_NO")
    private String busiNo;

    @JacksonXmlProperty(localName = "IS_DELETE")
    private String isDelete;

    @JacksonXmlProperty(localName = "IMAGE_TYPE")
    private String docNo;



}
