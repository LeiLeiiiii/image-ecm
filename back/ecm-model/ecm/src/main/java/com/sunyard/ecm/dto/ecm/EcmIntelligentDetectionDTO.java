package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Wenbiwen
 */

@Data
@ApiModel("智能检测请求")
public class EcmIntelligentDetectionDTO {
    @ApiModelProperty(value = "文件ID", required = true)
    private Long fileId;

    @ApiModelProperty(value = "业务ID", required = true)
    private Long busiId;

    @ApiModelProperty(value = "最新的文件id")
    private Long newFileId;

    @ApiModelProperty(value = "类型 (1-文档识别, 2-自动转正, 3-模糊检测, 4-查重检测, 5-拆分合并, 6-翻拍检测 , 7-文本查重)", required = true)
    private Integer type;

    @ApiModelProperty(value = "类型 (质量检测类型3,8,9)", required = true)
    private List<Integer> types;

    @ApiModelProperty(value = "状态 (4-排除异常, 5-确认异常)", required = true)
    private String status;

}