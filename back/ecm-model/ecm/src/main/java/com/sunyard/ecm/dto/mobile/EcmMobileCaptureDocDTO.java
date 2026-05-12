package com.sunyard.ecm.dto.mobile;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lw
 * @since 2023/8/9 17:34
 * @Description 移动端获取资料列表入参
 */
@Data
public class EcmMobileCaptureDocDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务编号")
    private Long busiId;

    @ApiModelProperty(value = "资料类型名称")
    private String infoTypeName;

    @ApiModelProperty(value = "用户id")
    private String userId;

}
