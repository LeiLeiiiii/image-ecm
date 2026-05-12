package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 影像业务维度关联表
 * </p>
 *
 * @author zyl
 * @since 2023-04-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmAppDimension对象", description = "影像业务维度关联表")
public class EcmAppDimension implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "版本号")
    private Integer rightVer;

    @ApiModelProperty(value = "维度主键id")
    private Long dimId;

    @ApiModelProperty(value = "是否必选：0否，1是")
    private Integer required;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;


}
