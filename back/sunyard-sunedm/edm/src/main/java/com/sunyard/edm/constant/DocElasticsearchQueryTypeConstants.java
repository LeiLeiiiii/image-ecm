package com.sunyard.edm.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author wubingyang
 * @date 2021/11/23 8:52
 */
@Getter
@AllArgsConstructor
public enum DocElasticsearchQueryTypeConstants {
    //文档信息
    DOCUMENT(0, "文档信息", Arrays.asList("attachment.content", "docName"));

    private Integer code;

    private String desc;

    private List<String> fieldList;

}
