package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author scm
 * @since 2023/8/28 9:39
 * @desc 业务信息DTO
 */
@Data
public class EcmBusiAttrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 影像业务属性定义表主键
     */
    @JacksonXmlProperty(localName = "ID")
    @JsonProperty("appAttrId")
    private Long appAttrId;

    /**
     * 业务属性代码
     */
    @JacksonXmlProperty(localName = "CODE")
    @JsonProperty("attrCode")
    private String attrCode;

    /**
     * 业务属性名称
     */
    @JacksonXmlProperty(localName = "NAME")
    @JsonProperty("attrName")
    private String attrName;

    /**
     * 属性值
     */
    @JacksonXmlProperty(localName = "VALUE")
    @JsonProperty("appAttrValue")
    private String appAttrValue;

    /**
     * 属性顺序
     */
    @JacksonXmlProperty(localName = "SORT")
    @JsonProperty("attrSort")
    private Integer attrSort;

    /**
     * 是否主键(默认值为0；0：不作为业务主键1：作为业务主键)
     */
    @JacksonXmlProperty(localName = "IS_KEY")
    @JsonProperty("isKey")
    private Integer isKey;

    /**
     * 是否允许为空(默认值为0；0：不可为空；1：可为空；)
     */
    @JacksonXmlProperty(localName = "IS_NULL")
    @JsonProperty("isNull")
    private Integer isNull;
}
