package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm.receive
 * @Desc
 * @date 10:15 2021/11/16
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmDocInfo {

    @XmlElement(name = "BATCH_ID")
    private String batchId;

    @XmlElement(name = "BUSI_NUM")
    private String busiNum;

    @XmlElement(name = "BIZ_ORG")
    private String bizOrg;

    @XmlElement(name = "APP_CODE")
    private String appCode;

    @XmlElement(name = "APP_NAME")
    private String appName;

    @XmlElement(name = "BATCH_VER")
    private String batchVer;

    @XmlElement(name = "INTER_VER")
    private String interVer;

    @XmlElement(name = "STATUS")
    private String status;

    @XmlElement(name = "CREATE_USER")
    private String createUser;

    @XmlElement(name = "CREATE_DATE")
    private String createDate;

    @XmlElement(name = "MODIFY_USER")
    private String modifyUser;

    @XmlElement(name = "MODIFY_DATE")
    private String modifyDate;

    @XmlElement(name = "DOC_EXT")
    private EcmDocExt docExt;
}
