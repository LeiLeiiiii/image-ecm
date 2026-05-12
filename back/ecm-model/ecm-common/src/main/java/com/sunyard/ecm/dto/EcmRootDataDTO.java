package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author scm
 * @since 2023/8/10 14:29
 * @desc 对外接口传入参数DTO
 */
@Data
@JacksonXmlRootElement(localName = "root")
public class EcmRootDataDTO implements Serializable {
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
    private List<EcmBusExtendDTO> ecmBusExtendDTOS;

    /**
     * 这次会话的唯一值
     */
    String flagId;
}
