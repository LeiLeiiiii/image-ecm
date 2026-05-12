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
 * @date 2021/12/20 15:06
 * @Desc
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmPages implements Serializable {
    @XmlElement(name = "NODE")
    private List<EcmNode> node;

    @XmlElement(name = "PAGE")
    private List<String> pageid;
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/12/20 zhouleibin
 * creat
 */
