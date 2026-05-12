package com.sunyard.ecm.dto.ecm;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author XQZ
 * @date 2023/4/21
 * @describe 文件类型定义DTO类
 */
@Data
public class EcmFileTypeDefDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件类型id")
    private Long fileTypeId;

    @ApiModelProperty(value = "文件类型")
    private String fileTypeCode;

    @ApiModelProperty(value = "文件类型名称")
    private String fileTypeName;

    @ApiModelProperty(value = "最大上传文件(0：不限制（单位MB）)")
    private Long uploadSize;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @ApiModelProperty(value = "最新修改人")
    private String updateUser;

    @ApiModelProperty(value = "最新修改时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建人名称")
    private String createUserName;
    @ApiModelProperty(value = "最新修改人名称")
    private String updateUserName;
    @ApiModelProperty(value = "是否已被使用：0未使用，1已使用")
    private Integer isUse;
}
