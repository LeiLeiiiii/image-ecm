package com.sunyard.mytool.entity;




import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 系统用户实体类
 */
@TableName("sys_user")
public class SysUser {

    /**
     * 用户id
     */
    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long userId;

    /**
     * 机构id
     */
    @TableField("inst_id")
    private Long instId;

    /**
     * 部门id
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 登录名
     */
    @TableField("login_name")
    private String loginName;

    /**
     * 用户工号
     */
    @TableField("code")
    private String code;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 密码
     */
    @TableField("pwd")
    private String pwd;

    /**
     * 盐
     */
    @TableField("salt")
    private String salt;

    /**
     * 性别(女:0,男:1)
     */
    @TableField("sex")
    private Integer sex;

    /**
     * 联系方式
     */
    @TableField("phone")
    private String phone;

    /**
     * 电子邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 账号状态(未启用:0,启用:1,注销:2,锁定:3)
     */
    @TableField("state")
    private Integer state;

    /**
     * 账号类型(普通:0,经销商:1)
     */
    @TableField("type")
    private Integer type;

    /**
     * 第三方系统同步主键
     */
    @TableField("ldap_id")
    private String ldapId;

    /**
     * 是否有扫描仪权限 0是 1否
     */
    @TableField("is_scan")
    private Integer isScan;

    /**
     * 主题颜色
     */
    @TableField("theme_color")
    private String themeColor;

    /**
     * 框架布局
     */
    @TableField("frame_layout")
    private Integer frameLayout;

    /**
     * 是否折叠菜单（0否 1是）
     */
    @TableField("is_collapse")
    private Integer isCollapse;

    /**
     * 是否开启标签栏（0否 1是）
     */
    @TableField("is_label")
    private Integer isLabel;

    /**
     * 密码更新时间
     */
    @TableField("pwd_update_time")
    private Date pwdUpdateTime;

    /**
     * 用户自定义配置
     */
    @TableField("custom_config")
    private String customConfig;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1)
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getInstId() {
        return instId;
    }

    public void setInstId(Long instId) {
        this.instId = instId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getLdapId() {
        return ldapId;
    }

    public void setLdapId(String ldapId) {
        this.ldapId = ldapId;
    }

    public Integer getIsScan() {
        return isScan;
    }

    public void setIsScan(Integer isScan) {
        this.isScan = isScan;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public Integer getFrameLayout() {
        return frameLayout;
    }

    public void setFrameLayout(Integer frameLayout) {
        this.frameLayout = frameLayout;
    }

    public Integer getIsCollapse() {
        return isCollapse;
    }

    public void setIsCollapse(Integer isCollapse) {
        this.isCollapse = isCollapse;
    }

    public Integer getIsLabel() {
        return isLabel;
    }

    public void setIsLabel(Integer isLabel) {
        this.isLabel = isLabel;
    }

    public Date getPwdUpdateTime() {
        return pwdUpdateTime;
    }

    public void setPwdUpdateTime(Date pwdUpdateTime) {
        this.pwdUpdateTime = pwdUpdateTime;
    }

    public String getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(String customConfig) {
        this.customConfig = customConfig;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
