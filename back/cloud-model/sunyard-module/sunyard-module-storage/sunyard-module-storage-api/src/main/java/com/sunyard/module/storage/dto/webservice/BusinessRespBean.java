
package com.sunyard.module.storage.dto.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl
 * @Desc 接口参数
 * @date 17:09 2021/10/25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "businessRespBean", propOrder = {"appAttrList", "appCode", "appTypeList", "attrListList", "docList",
    "docRightList", "docTypeList", "fileTypeList", "rightIdList", "rightList", "roleCode", "roleList"})
public class BusinessRespBean extends ResponseBean {
    /**
     * 业务属性
     */
    protected String appAttrList;
    /**
     * 业务类型
     */
    protected String appCode;
    /**
     * 业务属性清单
     */
    protected String appTypeList;
    protected String attrListList;
    protected String docList;
    protected String docRightList;
    protected String docTypeList;
    protected String fileTypeList;
    protected String rightIdList;
    protected String rightList;
    protected String roleCode;
    protected String roleList;

    /**
     * 业务属性
     *
     * @return Result possible object is {@link String }
     */
    public String getAppAttrList() {
        return appAttrList;
    }

    /**
     * 业务属性
     *
     * @param value allowed object is {@link String }
     */
    public void setAppAttrList(String value) {
        this.appAttrList = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getAppCode() {
        return appCode;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setAppCode(String value) {
        this.appCode = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getAppTypeList() {
        return appTypeList;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setAppTypeList(String value) {
        this.appTypeList = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getAttrListList() {
        return attrListList;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setAttrListList(String value) {
        this.attrListList = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getDocList() {
        return docList;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setDocList(String value) {
        this.docList = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getDocRightList() {
        return docRightList;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setDocRightList(String value) {
        this.docRightList = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getDocTypeList() {
        return docTypeList;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setDocTypeList(String value) {
        this.docTypeList = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getFileTypeList() {
        return fileTypeList;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setFileTypeList(String value) {
        this.fileTypeList = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getRightIdList() {
        return rightIdList;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setRightIdList(String value) {
        this.rightIdList = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getRightList() {
        return rightList;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setRightList(String value) {
        this.rightList = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setRoleCode(String value) {
        this.roleCode = value;
    }

    /**
     * @return Result possible object is {@link String }
     */
    public String getRoleList() {
        return roleList;
    }

    /**
     * @param value allowed object is {@link String }
     */
    public void setRoleList(String value) {
        this.roleList = value;
    }

    @Override
    public String toString() {
        return "BusinessRespBean{" + "appAttrList='" + appAttrList + '\'' + ", appCode='" + appCode + '\''
            + ", appTypeList='" + appTypeList + '\'' + ", attrListList='" + attrListList + '\'' + ", docList='"
            + docList + '\'' + ", docRightList='" + docRightList + '\'' + ", docTypeList='" + docTypeList + '\''
            + ", fileTypeList='" + fileTypeList + '\'' + ", rightIdList='" + rightIdList + '\'' + ", rightList='"
            + rightList + '\'' + ", roleCode='" + roleCode + '\'' + ", roleList='" + roleList + '\'' + '}';
    }
}
