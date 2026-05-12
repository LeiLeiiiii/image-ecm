package com.sunyard.mytool.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 文件表实体类
 */
@TableName("ST_FILE")
public class StFile {

    /**
     * 主键id
     */
    @TableId(value = "ID")
    private Long id;

    /**
     * 文件原始名称
     */
    @TableField(value = "ORIGINAL_FILENAME")
    private String originalFilename;

    /**
     * 文件名
     */
    @TableField(value = "FILENAME")
    private String filename;

    /**
     * 文件扩展名
     */
    @TableField(value = "EXT")
    private String ext;

    /**
     * 文件大小
     */
    @TableField(value = "\"SIZE\"")
    private Long size;

    /**
     * 文件的key(桶下的文件路径)
     */
    @TableField(value = "OBJECT_KEY")
    private String objectKey;

    /**
     * 文件来源(使用spring:application:name)
     */
    @TableField(value = "FILE_SOURCE")
    private String fileSource;

    /**
     * 存储设备id
     */
    @TableField(value = "EQUIPMENT_ID")
    private Long equipmentId;

    /**
     * 源文件MD5
     */
    @TableField(value = "SOURCE_FILE_MD5")
    private String sourceFileMd5;

    /**
     * 目标文件MD5
     */
    @TableField(value = "FILE_MD5")
    private String fileMd5;

    /**
     * 上传人
     */
    @TableField(value = "CREATE_USER")
    private Long createUser;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private Date createTime;

    /**
     * 删除状态(否:0,是:1)
     */
    @TableField(value = "IS_DELETED")
    private Integer isDeleted;

    /**
     * 是否加密 （0否 1是）
     */
    @TableField(value = "IS_ENCRYPT")
    private Integer isEncrypt;

    /**
     * 加密密钥
     */
    @TableField(value = "ENCRYPT_KEY")
    private String encryptKey;

    /**
     * 加密算法,0是AES
     */
    @TableField(value = "ENCRYPT_TYPE")
    private Integer encryptType;

    /**
     * 加密的密文长度
     */
    @TableField(value = "ENCRYPT_LEN")
    private Long encryptLen;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getFileSource() {
        return fileSource;
    }

    public void setFileSource(String fileSource) {
        this.fileSource = fileSource;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getSourceFileMd5() {
        return sourceFileMd5;
    }

    public void setSourceFileMd5(String sourceFileMd5) {
        this.sourceFileMd5 = sourceFileMd5;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
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

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Integer getIsEncrypt() {
        return isEncrypt;
    }

    public void setIsEncrypt(Integer isEncrypt) {
        this.isEncrypt = isEncrypt;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public Integer getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(Integer encryptType) {
        this.encryptType = encryptType;
    }

    public Long getEncryptLen() {
        return encryptLen;
    }

    public void setEncryptLen(Long encryptLen) {
        this.encryptLen = encryptLen;
    }
}
