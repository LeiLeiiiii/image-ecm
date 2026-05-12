package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm
 * @Desc
 * @date 15:01 2021/10/27
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmVtree implements Serializable {

    @XmlAttribute(name = "APP_CODE")
    private String appCode;

    @XmlAttribute(name = "APP_NAME")
    private String appName;

    @XmlElement(name = "NODE")
    private List<EcmVtreeNode> nodes;
}
