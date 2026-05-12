package com.sunyard.ecm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author 朱山成
 * @time 2024/6/5 15:23
 **/
@Data
@Accessors(chain = true)
public class AreaInfoDTO {
    @ApiModelProperty(value = "省名称")
    private String provName;
    @ApiModelProperty(value = "市名称")
    private String cityName;
    @ApiModelProperty(value = "区/县名称")
    private String districtName;
}
