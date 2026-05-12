package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author： zyl
 * @create： 2023/5/31 13:55
 * @Description：单证属性信息DTO类
 */
@Data
public class EcmDtdAttrInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "影像单证属性定义表主键")
    private Long dtdAttrId;

    @ApiModelProperty(value = "单证类型id")
    private Long dtdTypeId;

    @ApiModelProperty(value = "单证属性代码（ocr-key）")
    private String attrCode;

    @ApiModelProperty(value = "单证属性名称")
    private String attrName;

    @ApiModelProperty(value = "属性排序")
    private Integer attrSort;

    @ApiModelProperty(value = "校验表达式")
    private String regex;

    @ApiModelProperty(value = "是否查重(默认0,查重1)")
    private Integer checkVaule;

    @ApiModelProperty(value = "检索是否显示（(0：不展示；默认1：展示)）")
    private Integer isShow;

    @ApiModelProperty(value = "输入类型：1：输入项2：日期项3：下拉")
    private Integer inputType;

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

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;

    @ApiModelProperty(value = "最新修改人名称")
    private String updateUserName;
}
