package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: lw
 * @Date: 2023/7/31
 * @Description: 影像扫描入参
 */
@Data
public class EcmBusiBatchScanDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "当前操作业务类型id")
    private String appCode;

    @ApiModelProperty(value = "当前操作业务id")
    private Long busiId;

    @ApiModelProperty(value = "当前操作资料类型id")
    private String docId;

    @ApiModelProperty(value = "节点类型：1业务类型，2业务，3资料类型，4资料标记, 5未归类, 6已删除")
    Long type;

    @ApiModelProperty(value = "资料类型")
    String docCode;

    @ApiModelProperty(value = "业务id")
    List<Long> busiIdList ;

    @ApiModelProperty(value = "扫描仪参数业务类型id")
    List<EcmBusiScanInfoDTO> busiScanInfoDTOList;

    @ApiModelProperty(value = "页面标识")
    private String pageFlag;



}
