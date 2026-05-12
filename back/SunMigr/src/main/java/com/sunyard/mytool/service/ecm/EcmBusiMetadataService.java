package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.EcmBusiMetadata;

import java.util.List;


public interface EcmBusiMetadataService extends IService<EcmBusiMetadata> {
    EcmBusiMetadata selectByBusiIdAndAppAttrId(Long busiId , Long appAttrId);

    /**
     * 保存或更新业务属性
     */
    void saveOrUpdateMetada(List<EcmBusiMetadata> metaDataListToInsert, List<EcmBusiMetadata> metaDataListToUpdate);
}
