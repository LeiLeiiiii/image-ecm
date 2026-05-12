package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zyl
 * @since 2023/11/9 19:01
 * @Description 系统配置参数DTO类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysDictionaryDTO {
    @ApiModelProperty(value = "字段表主键")
    private String key;

    @ApiModelProperty(value = "系统类型")
    private Integer systemCode;
}
