package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 文件信息实体类
 */
@TableName("ecm_file_info")
public class EcmFileInfo {

    /**
     * 文件ID
     */
    @TableId(value = "file_id", type = IdType.NONE)
    private Long fileId;

    /**
     * 业务ID
     */
    @TableField("busi_id")
    private Long busiId;

    /**
     * 资料树主键
     */
    @TableField("doc_code")
    private String docCode;

    /**
     * 资料树标记主键
     */
    @TableField("mark_doc_id")
    private Long markDocId;

    /**
     * 最新的文件ID
     */
    @TableField("new_file_id")
    private Long newFileId;

    /**
     * 文件名称
     */
    @TableField("new_file_name")
    private String newFileName;

    /**
     * 文件唯一MD5（查重使用）
     */
    @TableField("file_md5")
    private String fileMd5;

    /**
     * 是否复用（默认0，1:复用）
     */
    @TableField("file_reuse")
    private Integer fileReuse;

    /**
     * 顺序（在doc_id下排序）
     */
    @TableField("file_sort")
    private Double fileSort;

    /**
     * 创建人
     */
    @TableField("create_user")
    private String createUser;

    /**
     * 创建时间，默认当前时间
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
     * 备注
     */
    @TableField("comment")
    private String comment;

    /**
     * 状态(默认正常展示:0,已删除:1)
     */
    @TableField("state")
    private Integer state;

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
     * 文件大小
     */
    @TableField("new_file_size")
    private Long newFileSize;

    /**
     * 拓展名,同st_file
     */
    @TableField("new_file_ext")
    private String newFileExt;

    /**
     * 已删除列表删除状态(否:0,是:1)
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 上传机构号
     */
    @TableField("org_code")
    private String orgCode;

    /**
     * 上传机构
     */
    @TableField("org_name")
    private String orgName;

    /**
     * 文件来源
     */
    @TableField("file_source")
    private String fileSource;

    /**
     * 文件唯一标识（新增字段）
     */
    @TableField("page_id")
    private String pageId;


    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getBusiId() {
        return busiId;
    }

    public void setBusiId(Long busiId) {
        this.busiId = busiId;
    }

    public String getDocCode() {
        return docCode;
    }

    public void setDocCode(String docCode) {
        this.docCode = docCode;
    }

    public Long getMarkDocId() {
        return markDocId;
    }

    public void setMarkDocId(Long markDocId) {
        this.markDocId = markDocId;
    }

    public Long getNewFileId() {
        return newFileId;
    }

    public void setNewFileId(Long newFileId) {
        this.newFileId = newFileId;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Integer getFileReuse() {
        return fileReuse;
    }

    public void setFileReuse(Integer fileReuse) {
        this.fileReuse = fileReuse;
    }

    public Double getFileSort() {
        return fileSort;
    }

    public void setFileSort(Double fileSort) {
        this.fileSort = fileSort;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
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

    public Long getNewFileSize() {
        return newFileSize;
    }

    public void setNewFileSize(Long newFileSize) {
        this.newFileSize = newFileSize;
    }

    public String getNewFileExt() {
        return newFileExt;
    }

    public void setNewFileExt(String newFileExt) {
        this.newFileExt = newFileExt;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getFileSource() {
        return fileSource;
    }

    public void setFileSource(String fileSource) {
        this.fileSource = fileSource;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }
}
