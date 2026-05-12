package com.sunyard.module.system.po;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 岗位表
 * </p>
 * @author wangmeiling
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysPost implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 岗位id
     */
    @TableId(value = "post_id", type = IdType.ASSIGN_ID)
    private Long postId;

    /**
     * 岗位代码
     */
    private String postCode;

    /**
     * 机构id
     */
    private Long instId;

    /**
     * 岗位名称
     */
    private String name;

    /**
     * 描述
     */
    private String remarks;

    /**
     * 系统区分(档案:0,影像:1)
     */
    private String systemCode;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 最近修改人
     */
    private String updateUser;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1)
     */
    @TableLogic
    private Integer isDeleted;

}
