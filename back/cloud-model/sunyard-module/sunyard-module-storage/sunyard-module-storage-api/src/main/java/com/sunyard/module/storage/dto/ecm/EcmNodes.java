package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * @Author 朱山成
 * @time 2022/6/2 14:33
 **/
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmNodes implements Serializable {
    @XmlElement(name = "NODE")
    private List<EcmNode> node;
}
