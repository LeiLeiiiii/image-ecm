package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm.receive
 * @Desc
 * @date 10:16 2021/11/16
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmDocExtAttr {

    @XmlAttribute(name = "ID")
    private String id;

    @XmlAttribute(name = "NAME")
    private String name;

    @XmlAttribute(name = "IS_SHOW")
    private String isShow;

    @XmlAttribute(name = "IS_KEY")
    private String isKey;

    @XmlAttribute(name = "IS_NULL")
    private String isNull;

    @XmlAttribute(name = "INPUT_TYPE")
    private String inputType;

    @XmlValue
    private String extAttrValue;
}
