package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author scm
 * @since 2023/8/21 16:55
 * @desc 静态数结构数据DTO
 */
@Data
public class EcmStaticTreeDataDTO implements Serializable {

    private static final long serialVersionUID = 5023396775407811193L;

    /**
     * 业务类型代码
     */
    @JacksonXmlProperty(isAttribute = true, localName = "APP_CODE")
    @JsonProperty("appCode")
    private String appCode;

    /**
     * 业务类型名称
     */
    @JacksonXmlProperty(isAttribute = true, localName = "APP_NAME")
    @JsonProperty("appName")
    private String appName;

}
