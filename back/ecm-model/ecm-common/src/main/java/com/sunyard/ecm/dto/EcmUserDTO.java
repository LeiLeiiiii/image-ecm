package com.sunyard.ecm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sunyard.ecm.RoleCodeDeserializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author scm
 * @since 2023/8/24 13:45
 * @desc 基本信息DTO
 */
@Data
public class EcmUserDTO implements Serializable {

    /**
     * 操作员代码
     */
    @JacksonXmlProperty(localName = "USER_CODE")
    @JsonProperty("userCode")
    private String userCode;

    /**
     * 操作员姓名
     */
    @JacksonXmlProperty(localName = "USER_NAME")
    @JsonProperty("userName")
    private String userName;

    /**
     * 机构代码|资料归属地
     */
    @JacksonXmlProperty(localName = "ORG_CODE")
    @JsonProperty("orgCode")
    private String orgCode;

    /**
     * 机构名称
     */
    @JacksonXmlProperty(localName = "ORG_NAME")
    @JsonProperty("orgName")
    private String orgName;

    /**
     * 缓存机构
     */
    @JacksonXmlProperty(localName = "COM_CODE")
    @JsonProperty("comCode")
    private String comCode;

    /**
     * 操作员角色
     */
    @JacksonXmlProperty(localName = "ROLE_CODE")
    @JsonProperty("roleCode")
    @JsonDeserialize(using = RoleCodeDeserializer.class) // 使用自定义反序列化器
    private List<String> roleCode;


}
