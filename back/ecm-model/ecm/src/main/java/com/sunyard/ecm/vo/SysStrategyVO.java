package com.sunyard.ecm.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author scm
 * @since 2023/7/28 14:21
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysStrategyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "OCR总识别开关")
    private Boolean ocrConfigStatus;

    @ApiModelProperty(value = "OCR总识别业务类型ID")
    private List<String> ocrConfigIds;

    @ApiModelProperty(value = "OCR识别开关")
    private Boolean ocrIdentifyStatus;

    @ApiModelProperty(value = "OCR识别业务类型ID")
    private List<String> ocrIdentifyIds;

    @ApiModelProperty(value = "OCR归类开关")
    private Boolean ocrSortStatus;

    @ApiModelProperty(value = "OCR归类业务类型ID")
    private List<String> ocrSortIds;

    @ApiModelProperty(value = "OCR图像纠偏")
    private Boolean ocrFlatStatus;

    @ApiModelProperty(value = "OCR转正业务类型ID")
    private List<String> ocrFlatIds;

    @ApiModelProperty(value = "混贴拆分开关")
    private Boolean splitStatus;

    @ApiModelProperty(value = "混贴拆分业务类型ID")
    private List<String> splitIds;

    @ApiModelProperty(value = "影像全局压缩开关")
    private Boolean zipStatus;

    @ApiModelProperty(value = "压缩界限")
    private Integer zipBound;

    @ApiModelProperty(value = "压缩比例")
    private Integer zipScale;

    @ApiModelProperty(value = "未归类文件提交")
    private String commit;

    @ApiModelProperty(value = "文件加密开关")
    private Boolean encryptStatus;

}
