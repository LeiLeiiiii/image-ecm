package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lw
 * @Description: 影像扫描入参
 * @Date: 2023/7/31
 */
@Data
public class EcmBusiSingleScanDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "扫描仪参数业务类型id")
    List<EcmBusiScanInfoDTO> busiScanInfoDTOList;

    @ApiModelProperty(value = "业务类型id")
    String appCode;

    @ApiModelProperty(value = "业务id")
    Long busiId;

    @ApiModelProperty(value = "资料类型id")
    String docId;

    @ApiModelProperty(value = "节点类型：1业务类型，2业务，3资料类型，4资料标记, 5未归类, 6已删除")
    Long type;

    @ApiModelProperty(value = "资料类型")
    String docCode;

}
