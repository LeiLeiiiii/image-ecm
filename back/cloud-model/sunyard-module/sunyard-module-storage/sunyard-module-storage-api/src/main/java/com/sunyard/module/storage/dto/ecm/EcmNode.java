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
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author zhouleibin
 * @date 2021/12/20 15:10
 * @Desc
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmNode implements Serializable {

    /**
     * 影像资料归类代码:资料类型
     */
    @XmlAttribute(name = "ID")
    private String id;
    /**
     * ADD：为新增 CLEAN_ADD：为整个资料分类下先删除再新增 MOD_ID：为根据PAGEID替换文件 DEL_ID：为根据PAGEID删除文件 操作
     */
    @XmlAttribute(name = "ACTION")
    private String action;

    @XmlElement(name = "PAGE")
    private List<EcmPage> page;

    /**
     * 影像资料归类代码:资料类型
     */
    @XmlElement(name = "NODE_ID")
    private String nodeId;
    /**
     * 资料类型名称
     */
    @XmlElement(name = "NODE_NAME")
    private String nodeName;
    /**
     * 资料个数
     */
    @XmlElement(name = "IMG_NUM")
    private String imgNum;
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/12/20 zhouleibin
 * creat
 */
