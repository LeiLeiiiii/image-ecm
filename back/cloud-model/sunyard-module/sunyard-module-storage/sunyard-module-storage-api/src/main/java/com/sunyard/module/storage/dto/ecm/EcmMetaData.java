package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm
 * @Desc
 * @date 14:57 2021/9/30
 */
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class EcmMetaData {
    /**
     * 业务数据
     */
    @XmlElement(name = "BATCH")
    private List<EcmBatch> batch;

    /**
     * 需要查看的资料
     */
    @XmlElement(name = "PAGEIDS")
    private EcmPageids ecmPageids;

    /**
     * 添加业务批次项
     *
     * @param metaDataBatch
     */
    public void addMetaDataBatch(EcmBatch metaDataBatch) {
        if (metaDataBatch == null) {
            return;
        }
        if (this.batch == null) {
            this.batch = new ArrayList<>();
        }
        this.batch.add(metaDataBatch);
    }
}
