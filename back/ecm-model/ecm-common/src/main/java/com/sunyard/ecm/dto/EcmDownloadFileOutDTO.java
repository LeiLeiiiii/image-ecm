package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;


/**
 * @author ypy
 * @since 2025/2/27
 */
@Data
@JacksonXmlRootElement(localName = "root")
@JsonInclude(JsonInclude.Include.NON_EMPTY)  // 忽略空字段，包括空字符串、空集合等
public class EcmDownloadFileOutDTO {

    /**
     * 基本信息
     */
    @JacksonXmlProperty(localName = "BASE_DATA")
    private EcmBaseInfoDTO ecmBaseInfoDTO;


    /**
     * 扩展信息（包含动态结构或者多维结构）
     */
    @JacksonXmlProperty(localName = "META_DATA")
    private EcmDownloadMetaDataDTO ecmDownloadMetaDataDTO;


}
