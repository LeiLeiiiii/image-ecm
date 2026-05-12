package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lw
 * @date 2023/7/31
 * @describe 业务缓存策略入参
 */
@Data
public class EcmBusiCacheStrategyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "清理时间")
    private String clearTime;

    @ApiModelProperty(value = "清理时间单位")
    private String clearTimeUnit;

    @ApiModelProperty(value = "清理阈值")
    private String clearThreshold;

    @ApiModelProperty(value = "清理阈值单位")
    private String clearThresholdUnit;



}
