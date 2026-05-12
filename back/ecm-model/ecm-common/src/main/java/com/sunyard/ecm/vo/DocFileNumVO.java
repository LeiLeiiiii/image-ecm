package com.sunyard.ecm.vo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class DocFileNumVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JacksonXmlProperty(localName = "NODE_ID")
    private String docCode;

    @JacksonXmlProperty(localName = "NODE_NAME")
    private String docName;

    @JacksonXmlProperty(localName = "IMG_NUM")
    private Integer fileSum;

}
