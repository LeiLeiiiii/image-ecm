package com.sunyard.mytool.dto;


import com.sunyard.mytool.entity.StEquipment;

import java.sql.Date;


public class EcmAppDefDto {
    private String appCode;
    private String appName;
    private Long equipmentId;
    private Integer arriveInform;
    private String queueName;
    private String parent;
    private double appSort;
    private Integer isQulity;
    private Integer resiz;
    private Double qulity;
    private String createUser;
    private Date createTime;
    private String updateUser;
    private Date updateTime;
    private String appTypeSign;
    private Integer typeSignStart;
    private Integer typeSignEnd;
    private Integer busiNoStart;
    private Integer busiNoEnd;

    private StEquipment stEquipment;

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

    public double getAppSort() {
        return appSort;
    }

    public void setAppSort(double appSort) {
        this.appSort = appSort;
    }

    public Integer getIsQulity() {
        return isQulity;
    }

    public void setIsQulity(Integer isQulity) {
        this.isQulity = isQulity;
    }

    public Integer getResiz() {
        return resiz;
    }

    public void setResiz(Integer resiz) {
        this.resiz = resiz;
    }

    public Double getQulity() {
        return qulity;
    }

    public void setQulity(Double qulity) {
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

    public StEquipment getStEquipment() {
        return stEquipment;
    }

    public void setStEquipment(StEquipment stEquipment) {
        this.stEquipment = stEquipment;
    }
}
