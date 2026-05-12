package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * AI桥接同步表实体
 *
 * @author 
 * @since 
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmAIBridgeSync对象", description = "AI桥接同步表")
@TableName("BUSINESS_AUTHORITY_TYPE_VIEW")
public class EcmAIBridgeSync implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分行名称")
    private String branchName;

    @ApiModelProperty(value = "银行编号")
    private String branchNumber;

    @ApiModelProperty(value = "流程类型")
    private String delegateType;

    @ApiModelProperty(value = "流程名称")
    private String delegateTypeName;

    @ApiModelProperty(value = "业务类型type")
    private String typeBig;

    @ApiModelProperty(value = "业务类型名称")
    private String typeBigName;

    @ApiModelProperty(value = "USER_ID")
    private String userId;

    @ApiModelProperty(value = "USER_SHOW_ID")
    private String userShowId;

    @ApiModelProperty(value = "USER_NAME")
    private String userName;
}