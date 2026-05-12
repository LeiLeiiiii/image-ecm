package com.sunyard.module.system.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 角色表
 * </p>
 * @author PJW
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色id
     */
    @TableId(value = "role_id", type = IdType.ASSIGN_ID)
    private Long roleId;

    /**
     * 角色代码
     */
    private String roleCode;

    /**
     * 机构id
     */
    private Long instId;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 描述
     */
    private String remarks;

    /**
     * 启用状态(启用:0,停用:1)
     */
    private Integer status;

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
