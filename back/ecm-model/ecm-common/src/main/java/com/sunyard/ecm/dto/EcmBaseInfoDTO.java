package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author： lw
 * @create： 2023/12/28 14:27
 * @desc: 扫描或修改基础信息
 */
@Data
public class EcmBaseInfoDTO extends EcmUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务控制参数
     */
    @JacksonXmlProperty(localName = "BIZ_CONTROL")
    @JsonProperty("bizControl")
    private String bizControl;

    /**
     * 扫描类型(0-批量扫描 1-单次扫描  默默1)
     */
    @JacksonXmlProperty(localName = "ONE_BATCH")
    @JsonProperty("oneBatch")
    private String oneBatch;

    /**
     * 模式(0静态树，1动态树)
     */
    @JacksonXmlProperty(localName = "tree_type")
    @JsonProperty("typeTree")
    private String typeTree;

    /**
     * 是否是查看页面，0：查看，1：采集
     */
    @JacksonXmlProperty(localName = "is_scan")
    @JsonProperty("isScan")
    private Integer isScan;

    /**
     * 是否补传
     */
    @JacksonXmlProperty(localName = "is_retransmission")
    @JsonProperty("isRetransmission")
    private Integer isRetransmission;

    /**
     * 静态结构树
     */
    @JacksonXmlElementWrapper(localName = "BIZ_INFO")
    @JacksonXmlProperty(localName = "BIZ_TYPE")
    @JsonProperty("ecmStaticTreeDataList")
    private List<EcmStaticTreeDataDTO> ecmStaticTreeDataList;

    private Map<String, Object> extraFields = new HashMap<>();

    // Getters and Setters

    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        extraFields.put(key, value);
    }
}
