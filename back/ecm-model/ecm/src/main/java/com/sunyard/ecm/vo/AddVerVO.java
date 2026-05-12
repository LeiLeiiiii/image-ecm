package com.sunyard.ecm.vo;

import com.sunyard.framework.common.page.PageForm;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ty
 * @since 2023-4-17 15:18
 * @desc 新增版本VO
 */
@Data
public class AddVerVO extends PageForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务类型id
     */
    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    /**
     * 版本号
     */
    @ApiModelProperty(value = "版本号")
    private Integer rightVer;

    /**
     * 版本说明
     */
    @ApiModelProperty(value = "版本说明")
    private String rightName;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createUser;

    /**
     * 新增版本类型（0：空白版本，1：复用已有版本）
     */
    @ApiModelProperty(value = "新增版本类型（0：空白版本，1：复用已有版本）")
    private Integer addVerType;

    /**
     * 选择版本
     */
    @ApiModelProperty(value = "选择版本")
    private Integer selectVerNo;

    /**
     * 最近修改人
     */
    @ApiModelProperty(value = "最近修改人")
    private String updateUser;

    /**
     * 业务资料权限版本id
     */
    @ApiModelProperty(value = "业务资料权限版本id")
    private Long id;

    /**
     * 维度类型：0角色维度，1业务多维度
     */
    @ApiModelProperty(value = "维度类型：0角色维度，1业务多维度")
    private Integer dimType;

    /**
     * 是否使用：0否1是
     */
    @ApiModelProperty(value = "是否使用：0否1是")
    private Integer isUse;
}
