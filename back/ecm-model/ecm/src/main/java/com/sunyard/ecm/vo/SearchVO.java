package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author XQZ
 * @date 2023/4/25
 * @describe 属性查询定义VO
 */
@Data
public class SearchVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "前端列表的code")
    private String code;

    @ApiModelProperty(value = "前端列表的name")
    private String label;

    @ApiModelProperty(value = "前端列表的placeholder")
    private String placeholder;

    @ApiModelProperty(value = "前端列表的type")
    private String type;

    @ApiModelProperty(value = "前端列表的option")
    private List<SearchOptionVO> option;

    @ApiModelProperty(value = "前端列表的datetype")
    private String datetype;

    @ApiModelProperty(value = "前端列表的valueFormat")
    private String valueFormat;

    @ApiModelProperty(value = "是否业务属性字段：否false 是true")
    private Boolean attrFlag;

    @ApiModelProperty(value = "业务属性字段id")
    private Long appAttrId;

    @ApiModelProperty(value = "是否允许为空(0：不可为空；1：可为空")
    private Integer isNull;

    @ApiModelProperty(value = "是否主键(默认值为0；0：不作为业务主键1：作为业务主键)")
    private Integer isKey;

    @ApiModelProperty(value = "校验表达式")
    private String regex;

    @ApiModelProperty(value = "属性顺序")
    private Integer attrSort;

    @ApiModelProperty(value = "是否归档标使(0否  1是)")
    private Integer isArchived;

    /**
     * 当前登录用户所属机构+用户选择的业务类型所属父节点代码+当前操作日期YYYYMMDD+五位随机数字
     */
    @ApiModelProperty(value = "业务属性主键默认值")
    private String keyValue;
}
