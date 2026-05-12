package com.sunyard.module.storage.ecm.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @Author 朱山成
 * @time 2022/8/2 17:07
 **/
@Data
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class NewEcmResponse implements Serializable {
    @XmlElement(name = "PAGES")
    private NewEcmPage page;
    @XmlElement(name = "RESPONSE_CODE")
    private String responseCode;
    @XmlElement(name = "RESPONSE_MSG")
    private String responseMsg;
}
