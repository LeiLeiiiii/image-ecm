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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouleibin
 * @date 2021/12/20 15:03
 * @Desc
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务类型
     */
    @XmlElement(name = "APP_CODE")
    private String appCode;

    /**
     * 业务类型名称
     */
    @XmlElement(name = "APP_NAME")
    private String appName;

    /**
     * 索引信息
     */
    @XmlElement(name = "BUSI_NO")
    private String busiNo;

    /**
     * 档案号
     */
    @XmlElement(name = "BUSI_ARCNO")
    private String busiArcno;

    /**
     * 版本号
     */
    @XmlElement(name = "QUERY_VER")
    private String queryVer;

    /**
     *
     * 索引信息（合约再保、临分再保特例）
     */
    @XmlElement(name = "businessNo")
    private String businessNo;

    /**
     * 档案标题
     */
    @XmlElement(name = "BUSI_TITLE")
    private String busiTitle;

    /**
     * 缓存机构的机构号
     */
    @XmlElement(name = "COM_CODE")
    private String comCode;
    /**
     * 动态属性
     */
    @XmlAnyElement
    private List<JAXBElement<String>> anyEle;

    /**
     * 图片信息(PAGES，最多一组，非必填，包含多组NODE，NODE下包含多组PAGE，PAGE下可传递PAGE_EXT)
     */
    @XmlElement(name = "PAGES")
    private EcmPages pages;

    @XmlElement(name = "VTREE")
    private EcmVtree ecmVtree;

    /**
     * 是否使用ES，0否 1是
     */
    @XmlElement(name = "IS_TOES")
    private String isToEs;

    /**
     * 添加属性
     *
     * @param appAttrName
     * @param appAttrValue
     */
    public void addAttr(String appAttrName, String appAttrValue) {
        if (this.anyEle == null) {
            this.anyEle = new ArrayList<>();
        }
        this.anyEle.add(new JAXBElement<>(new QName(appAttrName), String.class, appAttrValue));
    }
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/12/20 zhouleibin
 * creat
 */
