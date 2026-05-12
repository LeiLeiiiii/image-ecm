package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author scm
 * @since 2023/8/28 9:39
 * @desc 业务属性查询DTO
 */
@Data
public class EcmBusiAttrQueryDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务属性代码
     */
    @JacksonXmlProperty(localName = "CODE")
    @JsonProperty("attrCode")
    private String attrCode;
    /**
     * 属性值
     */
    @JacksonXmlProperty(localName = "VALUE")
    @JsonProperty("appAttrValue")
    private String appAttrValue;
}
