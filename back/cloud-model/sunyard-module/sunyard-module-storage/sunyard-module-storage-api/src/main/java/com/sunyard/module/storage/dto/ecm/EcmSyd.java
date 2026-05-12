package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm.receive
 * @Desc
 * @date 10:12 2021/11/16
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmSyd {

    @XmlElement(name = "doc")
    private EcmSydDoc doc;
}
