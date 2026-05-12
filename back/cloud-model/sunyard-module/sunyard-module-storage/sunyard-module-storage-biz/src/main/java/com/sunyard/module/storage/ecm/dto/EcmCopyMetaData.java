package com.sunyard.module.storage.ecm.dto;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author raochangmei
 * @Type com.sunyard.sunam.service.impl.sunecm
 * @Desc
 * @date 14:57 2021/9/30
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmCopyMetaData {
    /**
     * 来源节点
     */
    @XmlElement(name = "FROM_BATCH")
    private EcmCopyFromBatchData ecmCopyFromBatchData;

    /**
     * 业务数据
     */
    @XmlElement(name = "PAGE_COPY")
    private Integer pageCopy;

    /**
     * 目的节点
     */
    @XmlElement(name = "TO_BATCH")
    private EcmCopyToBatchData ecmCopyToBatchData;

}
