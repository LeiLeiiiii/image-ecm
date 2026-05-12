package com.sunyard.module.system.api.dto;
/*
 * Project: am
 *
 * File Created at 2021/7/15
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author zhouleibin
 */
@Data
public class SysUserAdminDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 机构id
     */
    private Long instId;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 登录名
     */
    private String loginName;

    /**
     * 用户工号
     */
    private String code;

    /**
     * 姓名
     */
    private String name;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 盐
     */
    private String salt;

    /**
     * 用户性别
     */
    private Integer sex;

    /**
     * 联系方式
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 账号状态(未启用:0,启用:1,注销:2,锁定:3)
     */
    private Integer state;

    /**
     * 账号类型(普通:0,系统管理员:1)
     */
    private Integer type;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1)
     */
    private Integer isDeleted;

    /**
     * 第三方系统同步主键
     */
    private String ldapId;

    /**
     * 是否有扫描仪权限 0是 1否
     */
    private Integer isScan;

    private String instName;
    private String instNo;
    private String deptName;
    private Long[] roleIds;
    private String[] roleNames;

    /**
     * 默认菜单
     */
    private String defaultMenu;

    /**
     * 主题颜色
     */
    private String themeColor;

    /**
     * 框架布局
     */
    private Integer frameLayout;

    /**
     * 是否折叠菜单（0否 1是）
     */
    private Integer isCollapse;

    /**
     * 是否开启标签栏（0否 1是）
     */
    private Integer isLabel;

    /**
     * 密码更新时间
     */
    private Date pwdUpdateTime;

    /**
     * 用户角色
     */
    private String roleStr;
}
