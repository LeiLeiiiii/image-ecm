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
 * @date 10:12 2021/11/16
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmSydDoc {

    @XmlAttribute(name = "version")
    private String version;

    @XmlElement(name = "DocInfo")
    private EcmDocInfo docInfo;

    @XmlElement(name = "PageInfo")
    private EcmPageInfos pageInfo;

    @XmlElement(name = "VTREE")
    private EcmVtree vtree;
}
