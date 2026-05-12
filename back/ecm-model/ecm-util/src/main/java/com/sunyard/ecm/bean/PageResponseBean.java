package com.sunyard.ecm.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class PageResponseBean {

    @JacksonXmlProperty(isAttribute = true,localName = "PAGEID")
    private String fileId;

    @JacksonXmlProperty(isAttribute = true,localName = "FILE_NO")
    private String fileNo;

    @JacksonXmlProperty(isAttribute = true,localName = "PAGE_URL")
    private String fileUrl;

    @JacksonXmlProperty(isAttribute = true,localName = "THUM_URL")
    private String thumUrl;

    @JacksonXmlProperty(isAttribute = true,localName = "NODE_ID")
    private String docCode;

    @JacksonXmlProperty(isAttribute = true,localName = "FILE_NAME")
    private String fileName;

    @JacksonXmlProperty(isAttribute = true,localName = "PAGE_VER")
    private String fileSort;
}
