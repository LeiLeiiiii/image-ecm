package com.sunyard.ecm.dto.ecm;

import com.sunyard.ecm.po.EcmFileLabel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EcmFileLabelDto implements Serializable {
    @ApiModelProperty(value = "业务表主键")
    private Long busiId;

    @ApiModelProperty(value = "文件列表")
    private List<Long> fileIdList;

    @ApiModelProperty(value = "标签列表")
    private List<EcmFileLabel> labels;
}
