package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * @author lw
 * @date 2023/4/26
 * @describe新增返回业务类型参数
 */
@Data
public class AppTypeVO implements   Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "业务主索引名称")
    private String mainIndexName;

    @ApiModelProperty(value = "业务主索引值")
    private String mainIndexValue;

    @ApiModelProperty(value = "业务id")
    private Long busiId;

    @ApiModelProperty(value = "页面flag")
    private String pageFlag;


}
