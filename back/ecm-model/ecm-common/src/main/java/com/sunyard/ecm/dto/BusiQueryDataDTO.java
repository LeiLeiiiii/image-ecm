package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yzy
 * @since 2025/02/20 13:45
 * @desc 资料复制DTO
 */
@Data
@JacksonXmlRootElement(localName = "root")
@JsonInclude(JsonInclude.Include.NON_EMPTY)  // 忽略空字段，包括空字符串、空集合等
public class BusiQueryDataDTO implements Serializable {

    @JacksonXmlProperty(localName = "BASE_DATA")
    private EcmBaseInfoDTO ecmBaseInfoDTO;

    @JacksonXmlProperty(localName = "META_DATA")
    private EcmQueryMetaDataDTO ecmQueryMetaDataDTO;


}
