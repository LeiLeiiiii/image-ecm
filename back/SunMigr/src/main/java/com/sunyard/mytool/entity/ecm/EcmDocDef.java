package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 资料类型定义表
 */

@TableName("ecm_doc_def")
public class EcmDocDef {

    /**
     * 资料代码code
     */
    @TableId(value = "doc_code", type = IdType.NONE)
    private String docCode;

    /**
     * 资料名称
     */
    @TableField("doc_name")
    private String docName;

    /**
     * 父资料类型code
     */
    @TableField("parent")
    private String parent;

    /**
     * 资料顺序
     */
    @TableField("doc_sort")
    private Float docSort;

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
     * 资料类型标识
     */
    @TableField("doc_type_sign")
    private String docTypeSign;

    /**
     * 资料类型标识起始位置
     */
    @TableField("type_sign_start")
    private Integer typeSignStart;

    /**
     * 资料类型标识结束位置
     */
    @TableField("type_sign_end")
    private Integer typeSignEnd;

    /**
     * 限制文档格式及大小
     */
    @TableField("office_limit")
    private String officeLimit;

    /**
     * 限制图片格式及大小
     */
    @TableField("img_limit")
    private String imgLimit;

    /**
     * 限制音频格式及大小
     */
    @TableField("audio_limit")
    private String audioLimit;

    /**
     * 限制视频格式及大小
     */
    @TableField("video_limit")
    private String videoLimit;

    /**
     * 限制其他格式及大小
     */
    @TableField("other_limit")
    private String otherLimit;

    /**
     * 文件最小可上传数量
     */
    @TableField("min_files")
    private Integer minFiles;

    /**
     * 文件最大可上传数量
     */
    @TableField("max_files")
    private Integer maxFiles;

    /**
     * 文稿类识别转正配置开关(关:0,开:1)
     */
    @TableField("is_regularized")
    private Integer isRegularized;

    /**
     * 图像模糊检测配置开关(关:0,开:1)
     */
    @TableField("is_obscured")
    private Integer isObscured;

    /**
     * 图像翻拍检测配置开关(关:0,开:1)
     */
    @TableField("is_remade")
    private Integer isRemade;

    /**
     * 图像查重检测配置开关(关:0,开:1)
     */
    @TableField("is_plagiarism")
    private Integer isPlagiarism;

    /**
     * 自动分类检测配置开关（关：0，开：1）
     */
    @TableField("is_auto_classified")
    private Integer isAutoClassified;

    /**
     * 自动分类标识
     */
    @TableField("auto_classification_id")
    private String autoClassificationId;

    /**
     * 是否父级节点(0:否   1:是)
     */
    @TableField("is_parent")
    private Integer isParent;

    public String getDocCode() {
        return docCode;
    }

    public void setDocCode(String docCode) {
        this.docCode = docCode;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public Float getDocSort() {
        return docSort;
    }

    public void setDocSort(Float docSort) {
        this.docSort = docSort;
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

    public String getDocTypeSign() {
        return docTypeSign;
    }

    public void setDocTypeSign(String docTypeSign) {
        this.docTypeSign = docTypeSign;
    }

    public Integer getTypeSignStart() {
        return typeSignStart;
    }

    public void setTypeSignStart(Integer typeSignStart) {
        this.typeSignStart = typeSignStart;
    }

    public Integer getTypeSignEnd() {
        return typeSignEnd;
    }

    public void setTypeSignEnd(Integer typeSignEnd) {
        this.typeSignEnd = typeSignEnd;
    }

    public String getOfficeLimit() {
        return officeLimit;
    }

    public void setOfficeLimit(String officeLimit) {
        this.officeLimit = officeLimit;
    }

    public String getImgLimit() {
        return imgLimit;
    }

    public void setImgLimit(String imgLimit) {
        this.imgLimit = imgLimit;
    }

    public String getAudioLimit() {
        return audioLimit;
    }

    public void setAudioLimit(String audioLimit) {
        this.audioLimit = audioLimit;
    }

    public String getVideoLimit() {
        return videoLimit;
    }

    public void setVideoLimit(String videoLimit) {
        this.videoLimit = videoLimit;
    }

    public String getOtherLimit() {
        return otherLimit;
    }

    public void setOtherLimit(String otherLimit) {
        this.otherLimit = otherLimit;
    }

    public Integer getMinFiles() {
        return minFiles;
    }

    public void setMinFiles(Integer minFiles) {
        this.minFiles = minFiles;
    }

    public Integer getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(Integer maxFiles) {
        this.maxFiles = maxFiles;
    }

    public Integer getIsRegularized() {
        return isRegularized;
    }

    public void setIsRegularized(Integer isRegularized) {
        this.isRegularized = isRegularized;
    }

    public Integer getIsObscured() {
        return isObscured;
    }

    public void setIsObscured(Integer isObscured) {
        this.isObscured = isObscured;
    }

    public Integer getIsRemade() {
        return isRemade;
    }

    public void setIsRemade(Integer isRemade) {
        this.isRemade = isRemade;
    }

    public Integer getIsPlagiarism() {
        return isPlagiarism;
    }

    public void setIsPlagiarism(Integer isPlagiarism) {
        this.isPlagiarism = isPlagiarism;
    }

    public Integer getIsAutoClassified() {
        return isAutoClassified;
    }

    public void setIsAutoClassified(Integer isAutoClassified) {
        this.isAutoClassified = isAutoClassified;
    }

    public String getAutoClassificationId() {
        return autoClassificationId;
    }

    public void setAutoClassificationId(String autoClassificationId) {
        this.autoClassificationId = autoClassificationId;
    }

    public Integer getIsParent() {
        return isParent;
    }

    public void setIsParent(Integer isParent) {
        this.isParent = isParent;
    }
}
