package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.EcmBusiDocRel;

import java.util.List;

public interface EcmBusiDocRelService extends IService<EcmBusiDocRel> {

    /**
     * 根据docId删除
     * @param docIdList
     */
    void deleteByDocIds(List<Long> docIdList);

    /**
     * 批量插入闭包表
     * @param docRelList
     */
    void saveList(List<EcmBusiDocRel> docRelList);
}
