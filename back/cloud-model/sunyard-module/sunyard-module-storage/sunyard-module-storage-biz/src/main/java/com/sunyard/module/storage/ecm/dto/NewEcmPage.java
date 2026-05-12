package com.sunyard.module.storage.ecm.dto;

import com.sunyard.module.storage.dto.ecm.NewPageBody;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;

/**
 * @Author 朱山成
 * @time 2022/8/2 18:01
 **/
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class NewEcmPage implements Serializable {
    @XmlElement(name = "PAGE")
    private NewPageBody newPageBody;
}
