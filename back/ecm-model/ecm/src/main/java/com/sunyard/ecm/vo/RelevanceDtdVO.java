package com.sunyard.ecm.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/5/10 8:57
 * @Description： 属性关联VO
 */
@Data
public class RelevanceDtdVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Long> dtdTypeIds;
    private String docCode;
}
