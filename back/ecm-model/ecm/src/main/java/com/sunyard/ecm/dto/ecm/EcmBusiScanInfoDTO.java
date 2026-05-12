package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lw
 * @Description: 影像扫描入参
 * @Date: 2023/7/31
 */
@Data
public class EcmBusiScanInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务代码")
    private String appCode;

    @ApiModelProperty(value = "资料类型代码")
    private String docCode;

    @ApiModelProperty(value = "文件名称（文件唯一标识）")
    private String filename;

    @ApiModelProperty(value = "业务主索引编号")
    private String busiNo;

}
