package com.sunyard.module.system.api.dto;
/*
 * Project: Sunyard
 *
 * File Created at 2023/5/6
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import lombok.Data;

import java.io.Serializable;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/6 10:22
 */
@Data
public class SysMenuDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单id
     */
    private Long menuId;

    /**
     * 父菜单id
     */
    private Long parentId;

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

}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/6 Leo creat
 */