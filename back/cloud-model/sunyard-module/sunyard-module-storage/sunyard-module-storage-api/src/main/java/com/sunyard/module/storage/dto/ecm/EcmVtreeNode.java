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
 * @date 15:08 2021/10/27
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmVtreeNode implements Serializable {

    @XmlElement(name = "NODE")
    List<EcmVtreeNode> nodes;
    @XmlElement(name = "LEAF")
    List<String> leafs;
    @XmlAttribute(name = "ID")
    private String id;
    @XmlAttribute(name = "NAME")
    private String name;
    @XmlAttribute(name = "RIGHT")
    private String right;
    @XmlAttribute(name = "RESEIZE")
    private String reseize = "800*600";
    @XmlAttribute(name = "CHILD_FLAG")
    private String childFlag = "1";
    @XmlAttribute(name = "BARCODE")
    private String barCode;
    @XmlAttribute(name = "MAXPAGES")
    private int maxPages = 999;
    @XmlAttribute(name = "MINPAGES")
    private int minPages = 0;
    private String arcId;
}
