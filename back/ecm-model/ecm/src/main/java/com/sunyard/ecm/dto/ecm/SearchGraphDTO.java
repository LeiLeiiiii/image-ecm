package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * @author ypy
 * @date 2025/3/27
 * @describe 图谱检索封装DTO类
 */
@Data
public class SearchGraphDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务信息")
    private String busiInfo;

    @ApiModelProperty(value = "业务id")
    private String busiId;
}
