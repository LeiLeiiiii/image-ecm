package com.sunyard.ecm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 影像文件到期信息表
 * </p>
 *
 * @author ypy
 * @since 2025-11-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EcmFileExpireInfo对象", description = "影像文件到期信息表")
public class EcmFileExpireInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "关联ecm_file_info表的文件唯一标识")
    private Long fileId;

    @ApiModelProperty(value = "文件到期日期")
    private Date expireDate;

    /**
     * 是否到期：0-未到期，1-已到期
     */
    private Integer isExpired;

    /**
     * 记录创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 记录最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
