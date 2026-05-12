package com.sunyard.ecm.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lw
 * @date 2023/7/31
 * @describe 扫描拍摄返回参数VO
 */
@Data
public class EcmBusiDealVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务id")
    private Long busiId;

    @ApiModelProperty(value = "业务编号")
    private String busiNo;

    @ApiModelProperty(value = "资料id")
    private String docId;

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "节点类型：1业务类型，2业务，3资料类型，4资料标记, 5未归类, 6已删除")
    Long type;

    @ApiModelProperty(value = "资料类型")
    @JsonInclude
    String docCode;


}
