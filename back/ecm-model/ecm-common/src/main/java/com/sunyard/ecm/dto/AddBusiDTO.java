package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author scm
 * @since 2023/8/24 16:20
 * @desc 添加业务入参DTO
 */
@Data
public class AddBusiDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 基本信息
     */
    @JacksonXmlProperty(localName = "BASE_DATA")
    @JsonProperty("ecmBaseInfoDTO")
    private EcmBaseInfoDTO ecmBaseInfoDTO;

    /**
     * 扩展信息（包含动态结构或者多维结构）
     */
    @JacksonXmlElementWrapper(localName = "META_DATA")
    @JacksonXmlProperty(localName = "BATCH")
    @JsonProperty("ecmBusExtendDTOS")
    private EcmBusExtendDTO ecmBusExtendDTOS;

}
