package com.sunyard.ecm.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "root")
public class Ecm0010ResponseBean {

    /**
     * 响应码
     */
    @JacksonXmlProperty(localName = "RESPONSE_CODE")
    private Integer code;

    /**
     * 响应信息
     */
    @JacksonXmlProperty(localName = "RESPONSE_MSG")
    private String msg;

    @JacksonXmlElementWrapper(localName = "PAGES")
    @JacksonXmlProperty(localName = "PAGE")
    private List<PageResponseBean> pages;
}
