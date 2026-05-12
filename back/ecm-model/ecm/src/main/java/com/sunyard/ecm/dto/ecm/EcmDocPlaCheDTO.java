package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class EcmDocPlaCheDTO {
    @ApiModelProperty(value = "时间范围(近N年)")
    private Integer frameYear;

    @ApiModelProperty(value = "相似度阈值")
    private Double fileSimilarity;

    @ApiModelProperty(value = "资料关联code名称集合")
    private List<EcmDocPlaRelNameDTO> relDocCodes;

    @ApiModelProperty(value = "资料关联name集合")
    private List<String> relDocNames;

    @ApiModelProperty(value = "查询类型")
    private Integer queryType;

}
