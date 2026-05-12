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
 * @date 2022/3/22 17:56
 * @Desc
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmResponsePage implements Serializable {

    @XmlAttribute(name = "MD5")
    private String md5;
    @XmlAttribute(name = "PAGEID")
    private String pageid;
    /**
     * 大图
     */
    @XmlAttribute(name = "PAGE_URL")
    private String pageUrl;
    /**
     * 缩略图
     */
    @XmlAttribute(name = "THUM_URL")
    private String thumUrl;
    @XmlAttribute(name = "FILE_NO")
    private String fileNo;
    @XmlAttribute(name = "PAGE_VER")
    private String pageVer;
    @XmlAttribute(name = "FILE_NAME")
    private String fileName;
    @XmlAttribute(name = "NODE_ID")
    private String nodeId;
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2022/3/22 zhouleibin creat
 */
