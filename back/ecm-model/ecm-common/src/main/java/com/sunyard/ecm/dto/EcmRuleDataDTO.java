package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author scm
 * @since 2023/8/10 14:29
 * @desc 多维度结构数据DTO
 */
@Data
public class EcmRuleDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 维度英文名称
     */
    @JacksonXmlProperty(localName = "CL_D_METHOD")
    @JsonProperty("dimensionCode")
    private String dimensionCode;

    /**
     * 维度值
     */
    @JacksonXmlProperty(localName = "CL_D_POPERTY")
    @JsonProperty("dimensionValue")
    private String dimensionValue;


}
