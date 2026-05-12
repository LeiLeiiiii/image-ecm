package com.sunyard.ecm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author wubingyang
 * @date 2021/11/23 8:52
 * @Desc ES查询类型常量配置
 */
@Getter
@AllArgsConstructor
public enum EcmElasticsearchQueryTypeEnum {

    /**
     * 业务信息
     */
    BUSINESS(0, "业务信息", Arrays.asList("busiNo","creatUserName", "updateUserName","appAttrs")),

    /**
     * 文档信息
     */
    DOCUMENT(1, "文档信息", Arrays.asList("fileName","creatUserName", "updateUserName","ocrInfo","attachment.content")),

    /**
     * 标签信息
     */
    LABEL(2, "标签信息", Arrays.asList("fileLabel")),

    /**
     * 单证信息
     */
    DTD(3, "单证信息", Arrays.asList("fileName","attachment.content","dtdTypeName"));

    private Integer code;

    private String desc;

    private List<String> fieldList;

}
