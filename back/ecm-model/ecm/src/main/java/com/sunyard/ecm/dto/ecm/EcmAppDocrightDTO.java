package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ty
 * @since 2023-4-18 11:09
 * @Desc 业务资料权限DTO类
 */
@Data
public class EcmAppDocrightDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "影像业务资料权限版本表主键")
    private Long id;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "版本号")
    private Integer rightVer;

    @ApiModelProperty(value = "版本说明")
    private String rightName;

    @ApiModelProperty(value = "最新默认(最新使用1)")
    private Integer rightNew;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;
    @ApiModelProperty(value = "最新修改人名称")
    private String updateUserName;
    @ApiModelProperty(value = "业务类型名称")
    private String appTypeName;
    @ApiModelProperty(value = "角色维度是否使用：0否，1是")
    private Integer roleDimUse;
    @ApiModelProperty(value = "业务多维度是否使用：0否，1是")
    private Integer lotDimUse;
}
