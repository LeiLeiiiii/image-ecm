package com.sunyard.ecm.vo;

import lombok.Data;

import java.util.List;

/**
 * @author： zyl
 * @create： 2023/7/14 17:21
 * @Description：删除单证属性VO
 */
@Data
public class DeleteDocumentAttrVO {
    /**
     * 单证属性id集合
     */
    private List<Long> dtdAttrId;

    /**
     * 要删除的单证属性的单证类型id
     */
    private Long dtdTypeId;
}
