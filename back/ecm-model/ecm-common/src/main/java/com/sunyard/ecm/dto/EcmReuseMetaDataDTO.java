package com.sunyard.ecm.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

/**
 * @author yzy
 * @since 2025/02/20 13:45
 * @desc 资料复用META_DATA
 */
@Data
public class EcmReuseMetaDataDTO {
    /**
     * 来源批次信息
     */
    @JacksonXmlProperty(localName = "FROM_BATCH")
    private EcmReuseBatchDTO fromBatch;

    /**
     * 目标批次信息
     */
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "TO_BATCH")
    private List<EcmReuseBatchDTO> toBatch;

    /**
     * 选中要复用的资料节点信息
     */
    @JacksonXmlElementWrapper(localName = "IMAGE_TYPES")
    @JacksonXmlProperty(localName = "IMAGE_TYPE")
    private List<String> docNos;
}
