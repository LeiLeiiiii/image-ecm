package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class EcmFileLabelExtendDTO implements Serializable {


    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "业务id")
    private Long busiId;

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "标签名称")
    private String labelName;

    @ApiModelProperty(value = "关联的标签")
    private Long labelId;

    @ApiModelProperty(value = "标签关联文件数量")
    private Integer fileCount;
}
