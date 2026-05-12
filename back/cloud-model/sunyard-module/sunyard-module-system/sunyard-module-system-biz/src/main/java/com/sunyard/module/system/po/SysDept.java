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
 * 部门表
 * </p>
 * @author PJW
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysDept implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 部门号
     */
    private String deptNo;

    /**
     * 部门id-父级
     */
    private Long parentId;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门名称-递归显示
     */
    private String nameLevel;

    /**
     * 层级
     */
    private Integer newlevel;

    /**
     * 第三方系统同步主键
     */
    private String ldapId;

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
