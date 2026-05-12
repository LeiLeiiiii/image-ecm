package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 影像标签VO类
 */
@Data
public class SysLabelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "标签名称")
    private String labelName;

    @ApiModelProperty(value = "标签id")
    private Long labelId;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "上级标签id")
    private Long parentId;

    @ApiModelProperty(value = "标签类型（0：基本标签，1：自定义标签）")
    private Integer labelType;

    @ApiModelProperty(value = "最后层级（0：是，1：否）")
    private Integer lastLevel;

    @ApiModelProperty(value = "是否显示（0：不显示，1：显示）")
    private Integer showFlag;
}
