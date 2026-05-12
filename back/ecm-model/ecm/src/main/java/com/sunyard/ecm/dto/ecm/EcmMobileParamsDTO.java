package com.sunyard.ecm.dto.ecm;

import com.sunyard.module.system.api.dto.SysParamDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author： lw
 * @create： 2023/8/21 14:30
 * @desc: 移动端参数DTO类
 */
@Data
public class EcmMobileParamsDTO extends SysParamDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "页面业务缓存key(进入移动端使用)")
    private String pageBusiListKey;
    @ApiModelProperty(value = "页面唯一id")
    private String pageFlag;
}
