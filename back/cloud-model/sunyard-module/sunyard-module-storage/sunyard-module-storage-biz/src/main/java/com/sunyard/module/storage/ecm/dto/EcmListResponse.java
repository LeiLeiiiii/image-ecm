package com.sunyard.module.storage.ecm.dto;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential InformationVO"). You shall
 * not disclose such Confidential InformationVO and shall use it only in accordance with the terms of the license.
 */

import com.sunyard.module.storage.dto.ecm.EcmResponsePages;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author zhouleibin
 * @date 2021/12/21 13:53
 * @Desc
 */
@Data
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmListResponse implements Serializable {
    @XmlElement(name = "PAGES")
    private EcmResponsePages pages;

    @XmlElement(name = "RESPONSE_CODE")
    private String responseCode;
    @XmlElement(name = "RESPONSE_MSG")
    private String responseMsg;

}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/12/21 zhouleibin
 * creat
 */
