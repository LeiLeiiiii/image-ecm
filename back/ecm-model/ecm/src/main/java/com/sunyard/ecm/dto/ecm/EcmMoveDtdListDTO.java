package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author： lw
 * @create： 2023/8/11
 * @Description： 单证列表移动DTO类
 */
@Data
public class EcmMoveDtdListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "原单证类型id")
    private Long dtdTypeId;

    @ApiModelProperty(value = "目标单证类型前一个元素顺序")
    private Float targetUpDtdSort;

    @ApiModelProperty(value = "目标单证类型后一个元素顺序")
    private Float targetDownDtdSort;


    @ApiModelProperty(value = "资料类型id")
    private String docCode;
}
