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
 * 用户表
 * </p>
 * @author PJW
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
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
     * 性别(女:0,男:1)
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

    /**
     * 第三方系统同步主键
     */
    private String ldapId;

    /**
     * 是否有扫描仪权限 0是 1否
     */
    private Integer isScan;

    /**
     * 主题颜色
     */
    private String themeColor;

    /**
     * 框架布局
     */
    private Integer frameLayout;

    /**
     * 密码更新时间
     */
    private Date pwdUpdateTime;

    /**
     * 是否折叠菜单（0否 1是）
     */
    private Integer isCollapse;

    /**
     * 是否开启标签栏（0否 1是）
     */
    private Integer isLabel;

    /**
     * 是否开启标签栏（0否 1是）
     */
    private String customConfig;

}
