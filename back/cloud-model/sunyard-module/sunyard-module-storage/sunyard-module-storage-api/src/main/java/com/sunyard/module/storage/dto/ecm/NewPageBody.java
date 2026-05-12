package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

/**
 * @Author 朱山成
 * @time 2022/8/2 18:11
 **/
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class NewPageBody implements Serializable {
    @XmlAttribute(name = "PAGEID")
    private String pageid;
    @XmlAttribute(name = "FILE_NAME")
    private String fileName;
    @XmlAttribute(name = "NODE_ID")
    private String nodeId;
    @XmlAttribute(name = "NODE_NAME")
    private String nodeName;
}
