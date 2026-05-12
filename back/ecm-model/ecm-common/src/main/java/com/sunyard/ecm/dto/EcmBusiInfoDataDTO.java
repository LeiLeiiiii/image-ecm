package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.io.Serializable;

@Data
@JacksonXmlRootElement(localName = "root")
public class EcmBusiInfoDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 基本信息
     */
    @JacksonXmlProperty(localName = "BASE_DATA")
    @JsonProperty("ecmUserDTO")
    private EcmUserDTO ecmUserDTO;

    /**
     * 影像业务信息
     */
    @JacksonXmlElementWrapper(localName = "META_DATA")
    @JacksonXmlProperty(localName = "BATCH")
    @JsonProperty("ecmBusiInfoDTO")
    private EcmBusiInfoDTO ecmBusiInfoDTO;
}