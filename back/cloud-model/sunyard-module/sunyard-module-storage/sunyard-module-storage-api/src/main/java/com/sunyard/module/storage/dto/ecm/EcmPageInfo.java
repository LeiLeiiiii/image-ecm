package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm.receive
 * @Desc
 * @date 10:58 2021/11/16
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmPageInfo {

    @XmlAttribute(name = "PAGEID")
    private String pageId;

    @XmlElement(name = "CREATE_USER")
    private String createUser;

    @XmlElement(name = "CREATE_USERNAME")
    private String createUserName;

    @XmlElement(name = "CREATE_TIME")
    private String createTime;

    @XmlElement(name = "MODIFY_USER")
    private String modifyUser;

    @XmlElement(name = "MODIFY_TIME")
    private String modifyTime;

    @XmlElement(name = "PAGE_URL")
    private String pageUrl;

    @XmlElement(name = "THUM_URL")
    private String thumUrl;

    @XmlElement(name = "IS_LOCAL")
    private String isLocal;

    @XmlElement(name = "PAGE_VER")
    private String pageVer;

    @XmlElement(name = "PAGE_DESC")
    private String pageDesc;

    @XmlElement(name = "UPLOAD_ORG")
    private String uploadOrg;

    @XmlElement(name = "PAGE_CRC")
    private String pageCrc;

    @XmlElement(name = "PAGE_SIZE")
    private String pageSize;

    @XmlElement(name = "PAGE_FORMAT")
    private String pageFormat;

    @XmlElement(name = "PAGE_ENCRYPT")
    private String pageEncrypt;

    @XmlElement(name = "ORIGINAL_NAME")
    private String originalName;
}
