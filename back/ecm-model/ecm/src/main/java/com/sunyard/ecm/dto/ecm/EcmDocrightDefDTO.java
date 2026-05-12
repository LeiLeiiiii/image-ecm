package com.sunyard.ecm.dto.ecm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ty
 * @since 2023-4-18 17:46
 * @desc 资料权限DTO类
 */
@Data
public class EcmDocrightDefDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "影像资料权限定义表主键")
    private Long docrightId;

    @ApiModelProperty(value = "角色值或维度值串(按关联顺序组值)")
    private String roleDimVal;

    @ApiModelProperty(value = "业务类型id")
    private String appCode;

    @ApiModelProperty(value = "资料类型")
    private String docCode;

    @ApiModelProperty(value = "存储设备id")
    private Long equipmentId;

    @ApiModelProperty(value = "版本号")
    private Integer rightVer;

    @ApiModelProperty(value = "维度类型：0角色维度，1业务多维度")
    private Integer dimType;

    @ApiModelProperty(value = "是否使用：0否，1是")
    private Integer isUse;

    @ApiModelProperty(value = "新增权限（0：无新增权限1：有新增权限）")
    private String addRight;

    @ApiModelProperty(value = "查看权限（0：无查看权限1：有查看权限）")
    private String readRight;

    @ApiModelProperty(value = "修改权限（0：无修改权限1：有修改权限）")
    private String updateRight;

    @ApiModelProperty(value = "删除权限（0：无删除权限1：有删除权限）")
    private String deleteRight;

    @ApiModelProperty(value = "查看缩略图权限（0：无查看缩略图权限1：有查看缩略图权限）")
    private String thumRight;

    @ApiModelProperty(value = "打印权限（0：无打印权限1：有打印权限）")
    private String printRight;

    @ApiModelProperty(value = "下载权限（0：无下载权限1：有下载权限）")
    private String downloadRight;

    @ApiModelProperty(value = "他人修改，0:无法修改；1:可修改")
    private String otherUpdate;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最大数量")
    private Integer maxLen;

    @ApiModelProperty(value = "最小数量")
    private Integer minLen;

    @ApiModelProperty(value = "是否启用最大/最小数量限制")
    private String enableLenLimit;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "资料类型名称")
    private String docName;

    @ApiModelProperty(value = "改节点现有文件数量-用于校验最大上传数量")
    private Integer fileCount;
}
