package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lw
 * @date 2024/4/17
 * @describe 影像提交参数VO
 */
@Data
public class EcmSubmitVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务id集合")
    private List<Long> busiIdList;

    @ApiModelProperty(value = "页面变动表识(1-有变动 0-无变动)")
    private Integer changeFlag;

    @ApiModelProperty(value = "流程类型ID")
    private String delegateType;

    @ApiModelProperty(value = "流程类型NAME")
    private String delegateTypeName;

    @ApiModelProperty(value = "业务类型ID")
    private String typeBig;

    @ApiModelProperty(value = "业务类型NAME")
    private String typeBigName;

}