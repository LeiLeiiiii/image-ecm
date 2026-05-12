package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yzy
 * @since 2025/02/20 13:45
 * @desc 资料复用META_DATA
 */
@Data
public class EcmDownloadMetaDataDTO {
    /**
     * 来源批次信息
     */
    @JacksonXmlProperty(localName = "BATCH")
    private EcmDownloadBatchDTO ecmDownloadBatchDTO;

    /**
     * 选中要复用的资料节点信息
     */
    @JacksonXmlElementWrapper(localName = "IMAGE_TYPES")
    @JacksonXmlProperty(localName = "IMAGE_TYPE")
    private List<String> docNos;

    private Map<String, Object> extraFields = new HashMap<>();

    // Getters and Setters

    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        extraFields.put(key, value);
    }
}
