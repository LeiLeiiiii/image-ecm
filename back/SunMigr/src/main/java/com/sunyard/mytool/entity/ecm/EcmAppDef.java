package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 影像业务类型定义表
 */
@TableName("ecm_app_def")
public class EcmAppDef {

    /**
     * 业务类型code
     */
    @TableId(value = "app_code", type = IdType.NONE)
    private String appCode;

    /**
     * 业务名称
     */
    @TableField("app_name")
    private String appName;

    /**
     * 设备id
     */
    @TableField("equipment_id")
    private Long equipmentId;

    /**
     * 是否开启到达通知，0未开启，1开启
     */
    @TableField("arrive_inform")
    private Integer arriveInform;

    /**
     * 关联的消息队列名称
     */
    @TableField("queue_name")
    private String queueName;

    /**
     * 父业务类型code
     */
    @TableField("parent")
    private String parent;

    /**
     * 顺序
     */
    @TableField("app_sort")
    private Float appSort;

    /**
     * 是否压缩。（0：否，1：是）
     */
    @TableField("is_resize")
    private Integer isResize;

    /**
     * 压缩比例（例：800长宽都大于上述值时进行压缩，有一个不大于就不压缩）
     */
    @TableField("resize")
    private Integer resize;

    /**
     * 压缩质量(默认0.5)
     */
    @TableField("qulity")
    private Float qulity;

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
     * 业务类型标识
     */
    @TableField("app_type_sign")
    private String appTypeSign;

    /**
     * 业务类型标识起始位置
     */
    @TableField("type_sign_start")
    private Integer typeSignStart;

    /**
     * 业务类型标识结束位置
     */
    @TableField("type_sign_end")
    private Integer typeSignEnd;

    /**
     * 业务编号标识起始位置
     */
    @TableField("busi_no_start")
    private Integer busiNoStart;

    /**
     * 业务编号标识结束位置
     */
    @TableField("busi_no_end")
    private Integer busiNoEnd;

    /**
     * 是否父级目录(0:否   1:是)
     */
    @TableField("is_parent")
    private Integer isParent;

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public Integer getArriveInform() {
        return arriveInform;
    }

    public void setArriveInform(Integer arriveInform) {
        this.arriveInform = arriveInform;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public Float getAppSort() {
        return appSort;
    }

    public void setAppSort(Float appSort) {
        this.appSort = appSort;
    }

    public Integer getIsResize() {
        return isResize;
    }

    public void setIsResize(Integer isResize) {
        this.isResize = isResize;
    }

    public Integer getResize() {
        return resize;
    }

    public void setResize(Integer resize) {
        this.resize = resize;
    }

    public Float getQulity() {
        return qulity;
    }

    public void setQulity(Float qulity) {
        this.qulity = qulity;
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

    public String getAppTypeSign() {
        return appTypeSign;
    }

    public void setAppTypeSign(String appTypeSign) {
        this.appTypeSign = appTypeSign;
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

    public Integer getBusiNoStart() {
        return busiNoStart;
    }

    public void setBusiNoStart(Integer busiNoStart) {
        this.busiNoStart = busiNoStart;
    }

    public Integer getBusiNoEnd() {
        return busiNoEnd;
    }

    public void setBusiNoEnd(Integer busiNoEnd) {
        this.busiNoEnd = busiNoEnd;
    }

    public Integer getIsParent() {
        return isParent;
    }

    public void setIsParent(Integer isParent) {
        this.isParent = isParent;
    }
}
