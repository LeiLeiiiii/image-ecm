package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 影像业务信息表实体类
 */
@TableName("ecm_busi_info")
public class EcmBusiInfo {

    /**
     * 业务表主键
     */
    @TableId(value = "busi_id", type = IdType.NONE)
    private Long busiId;

    /**
     * 业务号
     */
    @TableField("busi_no")
    private String busiNo;

    /**
     * 业务类型code
     */
    @TableField("app_code")
    private String appCode;

    /**
     * 资料权限版本
     */
    @TableField("right_ver")
    private Integer rightVer;

    /**
     * 树标志(0静态树，1动态树，3静态有标记)
     */
    @TableField("tree_type")
    private Integer treeType;

    /**
     * 机构号
     */
    @TableField("org_code")
    private String orgCode;

    /**
     * 创建人
     */
    @TableField("create_user")
    private String createUser;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 最新修改人
     */
    @TableField("update_user")
    private String updateUser;

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

    /**
     * 创建者名称
     */
    @TableField("create_user_name")
    private String createUserName;

    /**
     * 更新者名称
     */
    @TableField("update_user_name")
    private String updateUserName;

    /**
     * 机构名称
     */
    @TableField("org_name")
    private String orgName;

    /**
     * 业务状态 '0待提交 1 已提交  2已受理（处理中）3 已作废 4 已退回 5 已完结'
     */
    @TableField("status")
    private Integer status;

    // Getter and Setter

    public Long getBusiId() {
        return busiId;
    }

    public void setBusiId(Long busiId) {
        this.busiId = busiId;
    }

    public String getBusiNo() {
        return busiNo;
    }

    public void setBusiNo(String busiNo) {
        this.busiNo = busiNo;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Integer getRightVer() {
        return rightVer;
    }

    public void setRightVer(Integer rightVer) {
        this.rightVer = rightVer;
    }

    public Integer getTreeType() {
        return treeType;
    }

    public void setTreeType(Integer treeType) {
        this.treeType = treeType;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
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

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
