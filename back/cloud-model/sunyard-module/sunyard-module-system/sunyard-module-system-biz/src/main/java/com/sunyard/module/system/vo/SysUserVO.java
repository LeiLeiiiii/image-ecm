package com.sunyard.module.system.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/**
 * @author huronghao
 * @Type
 * @Desc
 * @date 2023-05-24 9:00
 */
@Data
public class SysUserVO implements Serializable {
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

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 账号类型(普通:0,经销商:1)
     */
    private String instName;
    private String deptName;
    private String roleName;
    private Long[] roleIds;
    private Long[] postIds;
    private Long roleId;
    private String[] roleNames;
    private String roleNameCollect;
    private List<Long> userGroups;

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
     * 用户状态集
     */
    private List<Integer> stateList;

    /**
     * 自定义配置
     */
    private String customConfig;

}
