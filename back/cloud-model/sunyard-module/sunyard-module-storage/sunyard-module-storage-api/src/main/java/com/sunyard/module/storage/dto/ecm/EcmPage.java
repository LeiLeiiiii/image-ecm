package com.sunyard.module.storage.dto.ecm;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential InformationVO"). You shall
 * not disclose such Confidential InformationVO and shall use it only in accordance with the terms of the license.
 */

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

/**
 * @author zhouleibin
 * @date 2021/12/20 15:22
 * @Desc
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmPage implements Serializable {

    @XmlAttribute(name = "FILE_NO")
    private String fileNo;

    /**
     * 文件名称
     */
    @XmlAttribute(name = "FILE_NAME")
    private String fileName;
    /**
     * 影像代码
     */
    @XmlAttribute(name = "PAGEID")
    private String pageId;

    @XmlAttribute(name = "PAGE_VER")
    private String pageVer;

    @XmlAttribute(name = "PAGE_URL")
    private String pageUrl;

    @XmlAttribute(name = "THUM_URL")
    private String thumUrl;

    /**
     * 文件MD5
     */
    @XmlAttribute(name = "MD5")
    private String md5;
    /**
     * 上传用户编号
     */
    @XmlAttribute(name = "UP_USER")
    private String upUser;
    /**
     * 上传用户名称
     */
    @XmlAttribute(name = "UP_USER_NAME")
    private String upUserName;
    /**
     * 上传机构代码
     */
    @XmlAttribute(name = "UP_ORG")
    private String upOrg;
    /**
     * 上传机构名称
     */
    @XmlAttribute(name = "UP_ORGNAME")
    private String upOrgname;
    /**
     * 上传时间
     */
    @XmlAttribute(name = "UP_TIME")
    private String upTime;
    /**
     * 影像备注
     */
    @XmlAttribute(name = "REMARK")
    private String remark;

}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/12/20 zhouleibin
 * creat
 */
