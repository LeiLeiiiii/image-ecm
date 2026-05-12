package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author： ty
 * @create： 2023/4/28 14:27
 * @Desc: 业务类型定义DTO类
 */
@Data
@ApiModel(value = "EcmAppDef返回参数", description = "EcmAppDef返回参数")
public class EcmAppDefDTO implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "业务代码")
    private String appCode;

    @ApiModelProperty(value = "业务名称")
    private String appName;

    @ApiModelProperty(value = "子资料类型ids")
    private List<String> children;
}
