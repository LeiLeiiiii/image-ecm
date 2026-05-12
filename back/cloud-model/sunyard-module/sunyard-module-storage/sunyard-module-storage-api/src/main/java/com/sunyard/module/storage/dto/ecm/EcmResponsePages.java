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
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author zhouleibin
 * @date 2022/3/22 17:56
 * @Desc
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmResponsePages implements Serializable {

    @XmlElement(name = "PAGE")
    private List<EcmResponsePage> pages;
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2022/3/22 zhouleibin creat
 */
