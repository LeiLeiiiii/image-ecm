package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ypy
 * @since 2025/2/26
 * @desc 业务属性DTO
 */
@Data
@JacksonXmlRootElement(localName = "root")
@JsonInclude(JsonInclude.Include.NON_EMPTY)  // 忽略空字段，包括空字符串、空集合等
public class EditBusiAttrOutDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 基本信息
     */
    @JacksonXmlProperty(localName = "BASE_DATA")
    private EcmUserDTO ecmBaseInfoDTO;

    /**
     * 扩展信息（包含动态结构或者多维结构）
     */
    @JacksonXmlElementWrapper(localName = "META_DATA")
    @JacksonXmlProperty(localName = "BATCH")
    private List<EcmBusExtendDTO> ecmBusExtendDTOS;

    private Map<String, Object> extraFields = new HashMap<>();

    // Getters and Setters

    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        extraFields.put(key, value);
    }
}
