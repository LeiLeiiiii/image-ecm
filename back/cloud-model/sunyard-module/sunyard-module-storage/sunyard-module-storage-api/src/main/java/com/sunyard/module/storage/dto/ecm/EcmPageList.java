package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm.receive
 * @Desc
 * @date 10:03 2021/11/16
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmPageList {

    @XmlElement(name = "PAGE")
    List<EcmPage> pageList;
}
