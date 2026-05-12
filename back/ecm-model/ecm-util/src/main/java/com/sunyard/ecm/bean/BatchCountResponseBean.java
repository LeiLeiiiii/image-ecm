package com.sunyard.ecm.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
public class BatchCountResponseBean {

    /**
     * 响应码
     */
    @JacksonXmlProperty(localName = "SUM_IMGS")
    private Integer count;

    @JacksonXmlElementWrapper(localName = "NODES")
    @JacksonXmlProperty(localName = "NODE")
    private List<NodeResponseBean> nodeResponseBeans;


}
