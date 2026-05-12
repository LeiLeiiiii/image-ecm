package com.sunyard.ecm.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author lw
 * @date 2023/7/31
 * @describe 业务存储策略返回参数VO
 */
@Data
public class EcmBusiStorageListVO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "是否开启到达通知，0未开启，1开启")
    private Integer arriveInform;

    @ApiModelProperty(value = "关联的消息队列名称")
    private String queueName;

    @ApiModelProperty(value = "设备id")
    private Long equipmentId;

    @ApiModelProperty(value = "业务类型id")
    private List<String> appCodes;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "业务名称")
    private String appName;

    @ApiModelProperty(value = "存储方式(1-本地存储 2-云存储)")
    private Integer storageType;

    @ApiModelProperty(value = "存储设备id")
    private String storageDeviceId;

    @ApiModelProperty(value = "存储设备名称")
    private String storageDeviceName;

    @ApiModelProperty(value = "存储路径")
    private String storageUrl;

    @ApiModelProperty(value = "桶名")
    private String bucket;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最近修改时间")
    private Date updateTime;

}
