package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EcmFileLableVO  implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "资料代码")
    private List<String> docCode;

    @ApiModelProperty(value = "文件id")
    private Long fileId;

    @ApiModelProperty(value = "业务id")
    private Long busiId;

    @ApiModelProperty(value = "是否标记节点")
    private Boolean isMark;
}
