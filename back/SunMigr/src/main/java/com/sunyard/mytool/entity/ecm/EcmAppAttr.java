package com.sunyard.mytool.entity.ecm;




import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 影像业务属性定义实体类
 */
@TableName("ecm_app_attr")
public class EcmAppAttr {

    /**
     * 主键
     */
    @TableId(value = "app_attr_id", type = IdType.NONE)
    private Long appAttrId;

    /**
     * 业务类型code
     */
    @TableField(value = "app_code")
    private String appCode;

    /**
     * 业务属性代码
     */
    @TableField(value = "attr_code")
    private String attrCode;

    /**
     * 业务属性名称
     */
    @TableField(value = "attr_name")
    private String attrName;

    /**
     * 属性顺序
     */
    @TableField(value = "attr_sort")
    private Integer attrSort;

    /**
     * 输入类型：1：输入项 2：日期项 3：下拉
     */
    @TableField(value = "input_type")
    private Integer inputType;

    /**
     * 状态(默认值为1；0：无效 1：有效)
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 是否主键(默认值为0；0：不作为业务主键 1：作为业务主键)
     */
    @TableField(value = "is_key")
    private Integer isKey;

    /**
     * 是否允许为空(默认值为0；0：不可为空；1：可为空)
     */
    @TableField(value = "is_null")
    private Integer isNull;

    /**
     * 是否在批次树根节点显示(默认值为1；0：不显示；1：显示)
     */
    @TableField(value = "tree_show")
    private Integer treeShow;

    /**
     * 列表显示
     */
    @TableField(value = "list_show")
    private Integer listShow;

    /**
     * 是否查询显示（0：查询不显示，1：查询显示）
     */
    @TableField(value = "query_show")
    private Integer queryShow;

    /**
     * 默认值
     */
    @TableField(value = "default_value")
    private String defaultValue;

    /**
     * 校验表达式
     */
    @TableField(value = "regex")
    private String regex;

    /**
     * 下拉列表的值，json存储(LIST:表示从扩展表查询)
     */
    @TableField(value = "list_value")
    private String listValue;

    /**
     * 创建人
     */
    @TableField(value = "create_user")
    private String createUser;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 最新修改人
     */
    @TableField(value = "update_user")
    private String updateUser;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    public Long getAppAttrId() {
        return appAttrId;
    }

    public void setAppAttrId(Long appAttrId) {
        this.appAttrId = appAttrId;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getAttrCode() {
        return attrCode;
    }

    public void setAttrCode(String attrCode) {
        this.attrCode = attrCode;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public Integer getAttrSort() {
        return attrSort;
    }

    public void setAttrSort(Integer attrSort) {
        this.attrSort = attrSort;
    }

    public Integer getInputType() {
        return inputType;
    }

    public void setInputType(Integer inputType) {
        this.inputType = inputType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsKey() {
        return isKey;
    }

    public void setIsKey(Integer isKey) {
        this.isKey = isKey;
    }

    public Integer getIsNull() {
        return isNull;
    }

    public void setIsNull(Integer isNull) {
        this.isNull = isNull;
    }

    public Integer getTreeShow() {
        return treeShow;
    }

    public void setTreeShow(Integer treeShow) {
        this.treeShow = treeShow;
    }

    public Integer getListShow() {
        return listShow;
    }

    public void setListShow(Integer listShow) {
        this.listShow = listShow;
    }

    public Integer getQueryShow() {
        return queryShow;
    }

    public void setQueryShow(Integer queryShow) {
        this.queryShow = queryShow;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getListValue() {
        return listValue;
    }

    public void setListValue(String listValue) {
        this.listValue = listValue;
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


}
