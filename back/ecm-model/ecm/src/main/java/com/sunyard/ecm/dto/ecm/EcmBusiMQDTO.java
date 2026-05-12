package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lw
 * @date 2023/8/1
 * @describe 业务消息队列入参
 */
@Data
public class EcmBusiMQDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "开启状态(1-本地存储 2-云存储)")
    private Integer status;

    @ApiModelProperty(value = "队列名称")
    private String mqName;

    @ApiModelProperty(value = "队列地址")
    private String mqAddress;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最近修改人")
    private String updateUser;

    @ApiModelProperty(value = "最近修改时间")
    private Date updateTime;

}
