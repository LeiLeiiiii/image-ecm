package com.sunyard.module.storage.ecm.dto;

import com.sunyard.module.storage.dto.ecm.BusiData;
import com.sunyard.module.storage.dto.ecm.EcmBaseData;
import com.sunyard.module.storage.dto.ecm.EcmMetaData;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm
 * @Desc 影像查阅接口 xml参数对应的实体bean
 * @date 15:30 2021/9/30
 */
@Data
@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryImgBusiData implements BusiData {
    /**
     * 基础信息
     */
    @XmlElement(name = "BASE_DATA")
    private EcmBaseData ecmBaseData;

    /**
     * 操作人信息
     */
    @XmlElement(name = "META_DATA")
    private EcmMetaData ecmMetaData;
}
