package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
 * @desc: 业务扩展信息DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)  // 忽略空字段，包括空字符串、空集合等
public class EcmBusExtendDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务类型代码
     */
    @JacksonXmlProperty(localName = "APP_CODE")
    @JsonProperty("appCode")
    private String appCode;

    /**
     * 业务类型名称
     */
    @JacksonXmlProperty(localName = "APP_NAME")
    @JsonProperty("appName")
    private String appName;

    /**
     * 业务主索引
     */
    @JacksonXmlProperty(localName = "BUSI_NO")
    @JsonProperty("busiNo")
    private String busiNo;

    /**
     * 业务扩展索引
     */
    @JacksonXmlProperty(localName = "CUST_NO")
    @JsonProperty("extendNo")
    private String extendNo;

    /**
     * 业务属性
     */
    @JacksonXmlElementWrapper(localName = "ATTR_LIST")
    @JacksonXmlProperty(localName = "ATTR")
    @JsonProperty("ecmBusiAttrDTOList")
    private List<EcmBusiAttrDTO> ecmBusiAttrDTOList;

    /**
     * 业务查询开始时间
     */
    @JacksonXmlProperty(localName = "START_TIME")
    @JsonProperty("startTime")
    private String startTime;

    /**
     * 业务查询结束时间
     */
    @JacksonXmlProperty(localName = "END_TIME")
    @JsonProperty("endTime")
    private String endTime;

    /**
     * 资料归属地机构
     */
    @JacksonXmlProperty(localName = "ORG_CODE")
    @JsonProperty("orgCode")
    private String orgCode;

    /**
     * 缓存机构代码
     */
    @JacksonXmlProperty(localName = "COM_CODE")
    @JsonProperty("comCode")
    private String comCode;

    /**
     * 只允许本人修改(0-否 1-是)
     */
    @JacksonXmlProperty(localName = "ONLY_SELF_ALERT")
    @JsonProperty("onlySelfAlert")
    private String onlySelfAlert;

    /**
     * 是否强制归类(0:无控制，1:弱控制，2:强控制)
     */
    @JacksonXmlProperty(localName = "CLASSIFY_LIMIT")
    @JsonProperty("classifyLimit")
    private String classifyLimit;

    /**
     * 资料大小校验
     */
    @JacksonXmlProperty(localName = "FILE_SIZE")
    @JsonProperty("fileSize")
    private String fileSize;

    /**
     * 基础标的类型
     */
    @JacksonXmlProperty(localName = "SCOPE_GROUP")
    @JsonProperty("scopeGroup")
    private String scopeGroup;

    /**
     * 标的
     */
    @JacksonXmlProperty(localName = "SUB_MATTERS")
    @JsonProperty("subMatters")
    private String subMatters;

    /**
     * 业务控制参数
     */
    @JacksonXmlProperty(localName = "BIZ_CONTROL")
    @JsonProperty("bizControl")
    private String bizControl;

    /**
     * 是否压缩(1-是 0-否,默认0)
     */
    @JacksonXmlProperty(localName = "IS_COMPRESS")
    @JsonProperty("isCompress")
    private String isCompress;

    /**
     * 压缩大小
     */
    @JacksonXmlProperty(localName = "COMPRESS_SIZE")
    @JsonProperty("compressSize")
    private String compressSize;

    /**
     * 压缩质量
     */
    @JacksonXmlProperty(localName = "COMPRESS_VALUE")
    @JsonProperty("compressValue")
    private String compressValue;

    /**
     * 动态结构树
     */
    @JacksonXmlElementWrapper(localName = "VTREE")
    @JacksonXmlProperty(localName = "NODE")
    @JsonProperty("ecmVTreeDataDTOS")
    private List<EcmVTreeDataDTO> ecmVTreeDataDTOS;

    /**
     * 多维度结构  (文档上写明最多一组，所以用单个对象接受)
     */
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "RULE_DATA")
    @JsonProperty("ecmRuleDataDTO")
    private List<EcmRuleDataDTO> ecmRuleDataDTO;


    /**
     * 多维度结构  (文档上写明最多一组，所以用单个对象接受)
     */
    @JacksonXmlProperty(localName = "RULE_DATA_STR")
    @JsonProperty("ecmRuleData")
    private String ecmRuleData;

    /**
     * 指定查看的资料类型代码
     */
    @JacksonXmlElementWrapper(localName = "DOC_CODES")
    @JacksonXmlProperty(localName = "DOC_CODES")
    @JsonProperty("ecmDocCodes")
    private List<String> ecmDocCodes;


    /**
     * 指定查看的资料类型代码
     */
    @JacksonXmlElementWrapper(localName = "FILE_IDS")
    @JacksonXmlProperty(localName = "FILE_IDS")
    @JsonProperty("fileIds")
    private List<Long> fileIds;

    /**
     * 是否为外部文件分片上传（空或0为采集时新增业务，1为文件上传时新增业务）
     */
    @JacksonXmlProperty(localName = "IS_ADD")
    @JsonProperty("isAdd")
    private Integer isAdd;


    /**
     * 模式(0静态树，1动态树)
     */
    @JacksonXmlProperty(localName = "tree_type")
    @JsonProperty("typeTree")
    private String typeTree;

    /**
     * 资源请求接口专用，权限过滤方式，默认全量查询，0：全量，1：根据角色权限查询，2：根据多维度权限查询
     */
    @JacksonXmlProperty(localName = "TYPE_QUERY")
    @JsonProperty("typeQuery")
    private Integer typeQuery;

    /**
     * 业务系统来源
     */
    @JacksonXmlProperty(localName = "SOURCE_SYSTEM")
    @JsonProperty("sourceSystem")
    private String sourceSystem;

    /**
     * 流程类型ID
     */
    @JacksonXmlProperty(localName = "DELEGATE_TYPE")
    @JsonProperty("delegateType")
    private String delegateType;

    /**
     * 业务类型ID
     */
    @JacksonXmlProperty(localName = "TYPE_BIG")
    @JsonProperty("typeBig")
    private String typeBig;

    private Map<String, Object> extraFields = new HashMap<>();

    // Getters and Setters

    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        extraFields.put(key, value);
    }
}
