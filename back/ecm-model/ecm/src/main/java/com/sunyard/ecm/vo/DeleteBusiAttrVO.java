package com.sunyard.ecm.vo;

import lombok.Data;

import java.util.List;

/**
 * @author： zyl
 * @create： 2023/7/14 17:15
 * @Description： 删除业务属性VO
 */
@Data
public class DeleteBusiAttrVO {
    /**
     * 业务属性id集合
     */
    private List<Long> appAttrId;

    /**
     * 要删除的业务属性的业务类型id
     */
    private String appCode;
}
