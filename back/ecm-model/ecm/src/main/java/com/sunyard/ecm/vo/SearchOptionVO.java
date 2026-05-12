package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author XQZ
 * @date 2023/4/26
 * @describe 查询操作VO
 */
@Data
public class SearchOptionVO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "前端列表的label")
    private String label;

    @ApiModelProperty(value = "前端列表的code")
    private String code;

    @ApiModelProperty(value = "前端列表的value")
    private String value;

    @ApiModelProperty(value = "前端展示所用")
    private Long parentId;

    @ApiModelProperty(value = "前端展示所用")
    private Long id;

    @ApiModelProperty(value = "前端展示所用")
    private String name;

}
