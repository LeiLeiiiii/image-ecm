package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author P-JWei
 * @date 2023/4/25 17:42 @title：
 * @description:
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmReturnData {

    /**
     * 批次资料个数总和
     */
    @XmlElement(name = "SUM_IMGS")
    private String sumImgs;

    @XmlElement(name = "NODES")
    private EcmNodes nodes;

}
