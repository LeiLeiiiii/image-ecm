package com.sunyard.ecm.dto.ecm;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author： ty
 * @create： 2023/5/8 13:52
 * @Desc: 业务信息DTO类
 */
@Data
public class EcmBusiStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务状态")
    private Integer status;

    @ApiModelProperty(value = "业务数量")
    private Integer busiNum;

}
