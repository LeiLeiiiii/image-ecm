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
import java.util.List;

/**
 * @author zhouleibin
 */
@Data
public class SysUserDTO implements Serializable {

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
     * 岗位id
     */
    private Long postId;

    /**
     * 岗位名称
     */
    private String postName;
    /**
     * 部门id
     */
    private String deptCode;
    /**
     * 所属组织
     */
    private String organization;

    /**
     * 用户id 文档用
     */
    private Long relId;

    /**
     * 类型 文档用
     */
    private Integer relType;

    /**
     * 登录用户名
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

    private Date createTime;

    private Date updateTime;

    /**
     * 账号类型(普通:0,经销商:1)
     */
    private String instName;
    private String deptName;
    private String roleName;
    private List<Long> roleIdList;
    private List<String> roleCodeList;

    private Long[] roleIds;
    private Long[] postIds;
    private Long roleId;
    private String[] roleNames;
    private String roleNameCollect;
    private String[] postNames;

    private List<Long> userGroups;

    /**
     * 用户角色code列表
     */
    private List<String> roleCode;

    /**
     * 用户角色code
     */
    private String role;

    /**
     * 系统菜单根节点
     */
    private String menuRootPerms;

    /**
     * 默认菜单枚举key
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
     * 所拥有角色
     */
    private String roleStr;

    /**
     * 机构号
     */
    private String instNo;

    /**
     * 自定义配置
     */
    private String customConfig;
}
