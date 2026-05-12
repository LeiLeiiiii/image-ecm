package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)  // 忽略空字段，包括空字符串、空集合等
public class EcmBusiInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务类型代码
     */
    @JacksonXmlProperty(localName = "APP_CODE")
    @JsonProperty("appCode")
    private String appCode;

    /**
     * 业务主索引
     */
    @JacksonXmlProperty(localName = "BUSI_NO")
    @JsonProperty("busiNo")
    private String busiNo;

}
