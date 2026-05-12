package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lw
 * @date 2023/7/31
 * @describe 业务缓存DTO类
 */
@Data
public class EcmBusiStorageDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;

    @ApiModelProperty(value = "存储方式(1-本地存储 2-云存储)")
    private Integer storageType;

    @ApiModelProperty(value = "存储设备id")
    private Long storageDeviceId;

    @ApiModelProperty(value = "存储设备名称")
    private String storageDeviceName;

    @ApiModelProperty(value = "存储路径")
    private String storageUrl;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最近修改人")
    private String updateUser;

    @ApiModelProperty(value = "最近修改时间")
    private Date updateTime;

}
