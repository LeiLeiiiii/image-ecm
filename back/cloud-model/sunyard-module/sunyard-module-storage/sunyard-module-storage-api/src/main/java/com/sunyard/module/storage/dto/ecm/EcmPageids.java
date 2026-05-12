package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author raochangmei
 * @Desc
 * @date 2022/4/26 15:23
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmPageids {

    @XmlElement(name = "PAGEID")
    private List<String> pageid;

}
