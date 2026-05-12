package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author： zyl
 * @create： 2023/6/28 16:15
 * @Description：单证属性集合信息DTO类
 */
@Data
public class EcmDtdAttrMulDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "属性id数组")
    private List<Long> attrIdList;
    @ApiModelProperty(value = "类型id")
    private Long typeId;
}
