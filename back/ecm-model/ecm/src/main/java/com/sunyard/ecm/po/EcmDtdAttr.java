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
 * 影像单证属性定义表
 * </p>
 *
 * @author zyl
 * @since 2023-04-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmDtdAttr对象", description = "影像单证属性定义表")
public class EcmDtdAttr implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "dtd_attr_id", type = IdType.ASSIGN_ID)
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
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @ApiModelProperty(value = "ocr文档对接类型 2：瑞真 3：信雅达ocr")
    private Integer type;


}
