package com.sunyard.ecm.dto.redis;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lw
 * @since 2023/8/9 17:34
 * @Description 采集页面表示DTO类
 */
@Data
public class UserPageRedisDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "页面标识")
    private String pageFlag;

    @ApiModelProperty(value = "业务")
    private List<Long> busiId;

    @ApiModelProperty(value = "扫描类型（单扫或者批扫）")
    private String modelType;
}
