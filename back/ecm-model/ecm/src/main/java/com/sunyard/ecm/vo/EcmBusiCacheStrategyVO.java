package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lw
 * @date 2023/7/31
 * @describe 业务缓存策略返回参数VO
 */
@Data
public class EcmBusiCacheStrategyVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "清理时间")
    private String clearTime;

    @ApiModelProperty(value = "清理时间单位")
    private String clearTimeUnit;

    @ApiModelProperty(value = "清理阈值")
    private String clearThreshold;

    @ApiModelProperty(value = "清理阈值单位")
    private String clearThresholdUnit;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最近修改人")
    private String updateUser;

    @ApiModelProperty(value = "最近修改时间")
    private Date updateTime;

}
