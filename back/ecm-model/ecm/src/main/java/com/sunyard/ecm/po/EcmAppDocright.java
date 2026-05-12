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
 * 影像业务资料权限版本表
 * </p>
 *
 * @author zyl
 * @since 2023-04-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmAppDocright对象", description = "影像业务资料权限版本表")
public class EcmAppDocright implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "版本号")
    private Integer rightVer;

    @ApiModelProperty(value = "版本说明")
    private String rightName;

    @ApiModelProperty(value = "最新默认(最新使用1)")
    private Integer rightNew;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "业务多维度是否使用：0否，1是")
    private Integer lotDimUse;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
