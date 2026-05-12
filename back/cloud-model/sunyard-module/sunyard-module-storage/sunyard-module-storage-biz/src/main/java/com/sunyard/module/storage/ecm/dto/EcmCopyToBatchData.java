package com.sunyard.module.storage.ecm.dto;

import com.sunyard.module.storage.dto.ecm.BusiData;
import com.sunyard.module.storage.dto.ecm.EcmVtree;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm
 * @Desc 影像查阅接口 xml参数对应的实体bean
 * @date 15:30 2021/9/30
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmCopyToBatchData implements BusiData {

    /**
     *
     */
    @XmlElement(name = "APP_CODE")
    private String appCode;

    /**
     *
     */
    @XmlElement(name = "APP_NAME")
    private String appName;

    /**
     *
     */
    @XmlElement(name = "BUSI_NO")
    private String busiNo;

    /**
     * 最后一层级分类
     */
    @XmlElement(name = "IMAGE_TYPE")
    private String imageType;

    /**
     * 最后一层级分类
     */
    @XmlElement(name = "VTREE")
    private EcmVtree ecmVtree;

}
