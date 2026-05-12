package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author： lw
 * @create： 2023/8/11
 * @Description： 单证属性移动入参DTO类
 */
@Data
public class EcmMoveDtdAttrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "原属性id")
    private Long dtdAttrId;

    @ApiModelProperty(value = "移动目标位置属性id")
    private Long targetDtdAttrId;

    @ApiModelProperty(value = "原属性顺序")
    private Integer attrSort;

    @ApiModelProperty(value = "目标属性顺序")
    private Integer targetAttrSort;

    @ApiModelProperty(value = "选中单证类型id")
    private Long dtdTypeId;
}
