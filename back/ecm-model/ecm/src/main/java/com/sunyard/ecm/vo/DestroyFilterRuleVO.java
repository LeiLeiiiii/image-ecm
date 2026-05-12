package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author ypy
 * @date 2025/12/17
 * @describe 销毁过滤规则VO
 */
@Data
public class DestroyFilterRuleVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "业务id")
    private Long busiId;

    @ApiModelProperty(value = "资料id")
    private String docCode;

    @ApiModelProperty(value = "销毁类型(0:历史业务销毁;1:历史资料销毁;2:已删除销毁)")
    private Integer destroyType;

    @ApiModelProperty(value = "文件状态")
    private Integer state;

    @ApiModelProperty(value = "是否删除(0：未删除  1：删除)")
    private Integer isDeleted;

    @ApiModelProperty(value = "任务id")
    private Long taskId;

    @ApiModelProperty(value = "是否删除(0：未删除  1：删除)")
    private Long destroyListId;
}
