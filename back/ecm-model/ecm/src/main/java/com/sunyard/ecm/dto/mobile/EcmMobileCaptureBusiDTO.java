package com.sunyard.ecm.dto.mobile;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lw
 * @since 2023/8/9 17:34
 * @Description 移动端获取业务列表入参
 */
@Data
public class EcmMobileCaptureBusiDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "页面所有busiId集合")
    private List<Long> busiIds;

    @ApiModelProperty(value = "业务编号")
    private String busiNo;

}
