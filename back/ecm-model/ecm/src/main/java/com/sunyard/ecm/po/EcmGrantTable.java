package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 授权表
 * </p>
 *
 * @author yzy
 * @since 2025-05-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmGrantTable对象", description = "授权表")
public class EcmGrantTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    @ApiModelProperty(value = "授权ID")
    private String id;

    @ApiModelProperty(value = "授权服务")
    private byte[] grantService;

    @ApiModelProperty(value = "访问授权")
    private byte[] grantAccess;
}