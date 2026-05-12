package com.sunyard.mytool.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 存储设备实体类
 */
@TableName("ST_EQUIPMENT")
public class StEquipment {

    /**
     * 主键id
     */
    @TableId(value = "ID")
    private Long id;

    /**
     * 设备名
     */
    @TableField(value = "EQUIPMENT_NAME")
    private String equipmentName;

    /**
     * 设备编码
     */
    @TableField(value = "EQUIPMENT_CODE")
    private String equipmentCode;

    /**
     * 存储方式
     */
    @TableField(value = "STORAGE_TYPE")
    private Integer storageType;

    /**
     * 基础路径
     */
    @TableField(value = "BASE_PATH")
    private String basePath;

    /**
     * 自定义域名
     */
    @TableField(value = "DOMAIN_NAME")
    private String domainName;

    /**
     * 节点地址
     */
    @TableField(value = "STORAGE_ADDRESS")
    private String storageAddress;

    /**
     * 存储bucket
     */
    @TableField(value = "BUCKET")
    private String bucket;

    /**
     * 存储连接key
     */
    @TableField(value = "ACCESS_KEY")
    private String accessKey;

    /**
     * 存储连接密钥
     */
    @TableField(value = "ACCESS_SECRET")
    private String accessSecret;

    /**
     * 是否启用0未启用，1启用
     */
    @TableField(value = "STATUS")
    private Integer status;

    /**
     * 上传人id
     */
    @TableField(value = "CREATE_USER")
    private Long createUser;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "UPDATE_TIME")
    private Date updateTime;

    /**
     * 最近修改人
     */
    @TableField(value = "UPDATE_USER")
    private String updateUser;

    /**
     * 删除状态(否:0,是:1)
     */
    @TableField(value = "IS_DELETED")
    private Integer isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public Integer getStorageType() {
        return storageType;
    }

    public void setStorageType(Integer storageType) {
        this.storageType = storageType;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getStorageAddress() {
        return storageAddress;
    }

    public void setStorageAddress(String storageAddress) {
        this.storageAddress = storageAddress;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessSecret() {
        return accessSecret;
    }

    public void setAccessSecret(String accessSecret) {
        this.accessSecret = accessSecret;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
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

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}
