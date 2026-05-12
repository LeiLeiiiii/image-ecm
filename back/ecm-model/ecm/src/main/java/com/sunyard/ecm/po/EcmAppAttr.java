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
 * 影像业务属性定义表
 * </p>
 *
 * @author zyl
 * @since 2023-04-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmAppAttr对象", description = "影像业务属性定义表")
public class EcmAppAttr implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "app_attr_id", type = IdType.ASSIGN_ID)
    private Long appAttrId;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "业务属性代码")
    private String attrCode;

    @ApiModelProperty(value = "业务属性名称")
    private String attrName;

    @ApiModelProperty(value = "属性顺序")
    private Integer attrSort;

    @ApiModelProperty(value = "输入类型：1：输入项2：日期项3：下拉")
    private Integer inputType;

    @ApiModelProperty(value = "状态(默认值为1；0：无效1：有效)")
    private Integer status;

    @ApiModelProperty(value = "是否主键(默认值为0；0：不作为业务主键1：作为业务主键)")
    private Integer isKey;

    @ApiModelProperty(value = "是否允许为空(默认值为0；0：不可为空；1：可为空；)")
    private Integer isNull;

    @ApiModelProperty(value = "是否显示(默认值为1；0：不在批次树根节点显示；1：在批次树根节点显示；)")
    private Integer treeShow;

    @ApiModelProperty(value = "是否查询显示(默认值为1；0：不显示；1：显示；)")
    private Integer queryShow;

    @ApiModelProperty(value = "列表显示")
    private Integer listShow;

    @ApiModelProperty(value = "默认值")
    private String defaultValue;

    @ApiModelProperty(value = "校验表达式")
    private String regex;

    @ApiModelProperty(value = "下拉列表的值，json存储(LISTE:表示从扩展表查询)")
    private String listValue;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "是否归档标使(0否  1是)")
    private Integer isArchived;
}
