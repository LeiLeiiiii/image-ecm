package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI桥接返回结果VO
 *
 * @author 
 * @since 
 */
@Data
@ApiModel(value = "EcmAIBridgeResultVO", description = "AI桥接返回结果VO")
public class EcmAIBridgeResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分组标签")
    private String label;

    @ApiModelProperty(value = "分组值")
    private Integer value;

    @ApiModelProperty(value = "子节点列表")
    private List<EcmAIBridgeTypeVO> children;
}