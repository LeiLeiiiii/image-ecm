package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author： ty
 * @create： 2023/4/28 14:27
 * @Desc: 业务属性DTO类
 */
@Data
public class EcmAppAttrDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "影像业务属性定义表主键")
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
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "属性值")
    private String appAttrValue;

    @ApiModelProperty(value = "前端所用")
    private String label;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "最新修改人名称")
    private String updateUserName;

    @ApiModelProperty(value = "是否归档标使(0否  1是)")
    private Integer isArchived;
}
