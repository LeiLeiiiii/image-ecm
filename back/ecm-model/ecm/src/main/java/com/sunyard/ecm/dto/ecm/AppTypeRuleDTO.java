package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * @author lw
 * @date 2023/12/15
 * @describe 业务类型规则DTO类
 */
@Data
public class AppTypeRuleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务类型")
    private String appCode;

    @ApiModelProperty(value = "分类标识")
    private String appTypeSign;

    @ApiModelProperty(value = "业务类型标识起始位置")
    private Integer typeSignStart;

    @ApiModelProperty(value = "业务类型标识结束位置")
    private Integer typeSignEnd;

    @ApiModelProperty(value = "业务编号标识起始位置")
    private Integer busiNoStart;

    @ApiModelProperty(value = "业务编号标识起始位置")
    private Integer busiNoEnd;

    @ApiModelProperty(value = "截取后的业务类型code")
    private String subStrAppTypeSign;

    @ApiModelProperty(value = "截取后业务类型长度")
    private Integer subStrAppCodeLength;

    @ApiModelProperty(value = "截取后的业务编号code")
    private String subStrBusiNo;
}
