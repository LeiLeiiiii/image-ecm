package com.sunyard.ecm.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class NodeResponseBean{

    @JacksonXmlProperty(localName = "NODE_ID")
    private String nodeId;

    @JacksonXmlProperty(localName = "NODE_NAME")
    private String nodeName;

    @JacksonXmlProperty(localName = "IMG_NUM")
    private Integer imgNum;
}
