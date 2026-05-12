package com.sunyard.ecm.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "root")
public class ResponseBean  {

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

    public ResponseBean(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResponseBean() {
    }
}
