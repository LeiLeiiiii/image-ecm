package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 影像业务属性值表(批次属性扩展表)
 * </p>
 *
 * @author zyl
 * @since 2023-04-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmBusiMetadata对象", description = "影像业务属性值表(批次属性扩展表)")
public class EcmBusiMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "业务属性表主键")
    private Long appAttrId;

    @ApiModelProperty(value = "扩展属性值")
    private String appAttrVal;


}
