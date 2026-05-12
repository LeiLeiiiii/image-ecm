package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author scm
 * @since 2023/8/24 13:45
 * @desc 动态数结构数据DTO
 */
@Data
public class EcmVTreeDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 资料代码
     */
    @JacksonXmlProperty(isAttribute = true, localName = "ID")
    @JsonProperty("docCode")
    private String docCode;

    /**
     * 资料名称
     */
    @JacksonXmlProperty(isAttribute = true, localName = "NAME")
    @JsonProperty("docName")
    private String docName;

    /**
     * 资料权限
     */
    @JacksonXmlProperty(isAttribute = true, localName = "RIGHT")
    @JsonProperty("docRight")
    private String docRight;
    /**
     * 是否加密，0：不加密，1：加密
     */
    @JacksonXmlProperty(isAttribute = true, localName = "ENCRYPT")
    @JsonProperty("ENCRYPT")
    private Integer isEncrypt;

    /**
     * 影像资料压缩大小
     */
    @JacksonXmlProperty(isAttribute = true, localName = "RESEIZE")
    @JsonProperty("docReseize")
    private String docReseize;

    /**
     * 叶子节点(1-是子节点、0-父节点)
     */
    @JacksonXmlProperty(isAttribute = true, localName = "CHILD_FLAG")
    @JsonProperty("childFlag")
    private String childFlag;

    /**
     * 资料条码
     */
    @JacksonXmlProperty(isAttribute = true, localName = "BARCODE")
    @JsonProperty("barCode")
    private String barCode;

    /**
     * 资料最大上传数量
     */
    @JacksonXmlProperty(isAttribute = true, localName = "MAXPAGES")
    @JsonProperty("maxPages")
    private String maxPages;

    /**
     * 资料最小上传数量
     */
    @JacksonXmlProperty(isAttribute = true, localName = "MINPAGES")
    @JsonProperty("minPages")
    private String minPages;

    @JacksonXmlProperty(isAttribute = true, localName = "OFFICELIMIT")
    @JsonProperty("officeLimit")
    private String officeLimit;

    @JacksonXmlProperty(isAttribute = true, localName = "IMGLIMIT")
    @JsonProperty("imgLimit")
    private String imgLimit;

    @JacksonXmlProperty(isAttribute = true, localName = "AUDIOLIMIT")
    @JsonProperty("audioLimit")
    private String audioLimit;

    @JacksonXmlProperty(isAttribute = true, localName = "VIDEOLIMIT")
    @JsonProperty("videoLimit")
    private String videoLimit;

    @JacksonXmlProperty(isAttribute = true, localName = "OTHERLIMIT")
    @JsonProperty("otherLimit")
    private String otherLimit;

    /**
     * 资料顺序
     */
    @JacksonXmlProperty(isAttribute = true, localName = "SORT")
    @JsonProperty("docOrder")
    private String docOrder;

    //@JacksonXmlElementWrapper(localName = "NODE")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "NODE")
    @JsonProperty("ecmVTreeDataDTOS")
    private List<EcmVTreeDataDTO> ecmVTreeDataDTOS;

}
