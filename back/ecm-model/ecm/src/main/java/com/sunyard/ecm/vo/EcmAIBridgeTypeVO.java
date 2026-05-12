package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI桥接类型VO
 *
 * @author 
 * @since 
 */
@Data
@ApiModel(value = "EcmAIBridgeTypeVO", description = "AI桥接类型VO")
public class EcmAIBridgeTypeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "类型代码")
    private String value;

    @ApiModelProperty(value = "类型名称")
    private String label;

    @ApiModelProperty(value = "子节点列表")
    private List<EcmAIBridgeTypeVO> children;
}