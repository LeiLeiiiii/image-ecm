package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 关联资料节点类
 */
@Data
public class EcmDocPlaRelNameDTO {
    @ApiModelProperty(value = "关联查重资料code")
    private String relDoccode;

    @ApiModelProperty(value = "关联查重资料类型")
    private Integer relType;

    @ApiModelProperty(value = "关联查重资料名称")
    private String relDocName;
}
