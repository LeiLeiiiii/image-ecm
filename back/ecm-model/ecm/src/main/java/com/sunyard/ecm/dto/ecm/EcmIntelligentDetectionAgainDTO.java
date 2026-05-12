package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author yzy
 */

@Data
public class EcmIntelligentDetectionAgainDTO {
    @ApiModelProperty(value = "文件ID", required = true)
    private Long fileId;

    @ApiModelProperty(value = "业务ID", required = true)
    private Long busiId;

    @ApiModelProperty(value = "最新的文件id")
    private Long newFileId;

    @ApiModelProperty(value = "资料code", required = true)
    private String docCode;

    @ApiModelProperty(value = "appCode", required = true)
    private String appCode;

    @ApiModelProperty(value = "类型 (1-单证识别, 2-自动转正, 3-模糊检测, 4-查重检测, 5-拆分合并, 6-翻拍检测)", required = true)
    private List<Integer> types;

}