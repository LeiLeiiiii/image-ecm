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
 * 系统菜单表
 * </p>
 * @author PJW
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单id
     */
    @TableId(value = "menu_id", type = IdType.ASSIGN_ID)
    private Long menuId;

    /**
     * 父菜单id
     */
    private Long parentId;

    /**
     * 菜单所属系统（0基础、1会计、2企业、3影像）
     */
    private Integer menuSystem;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 权限标识
     */
    private String perms;

    /**
     * 接口地址
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 菜单类型(菜单:M,目录:D,按钮:B)
     */
    private String menuType;

    /**
     * 是否为外链(否:0,是:1)
     */
    private Integer isFrame;

    /**
     * 是否缓存(不缓存:0,缓存:1)
     */
    private Integer isCache;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 启用状态(显示:0,停用:1)
     */
    private Integer status;

    /**
     * 隐藏状态(显示:0,隐藏:1)
     */
    private Integer visible;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 备注
     */
    private String remark;

    /**
     * 菜单归属权限（默认0 超级管理员9）
     */
    private Integer permissionsType;

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
