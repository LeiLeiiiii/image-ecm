package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.EcmBusiDoc;

import java.util.List;

public interface EcmBusiDocService extends IService<EcmBusiDoc> {

    /**
     * 根据busiid获取动态树信息
     * @param busiId
     * @return
     */
    List<EcmBusiDoc> getByBusiId(Long busiId);

    /**
     * 根据busiId删除动态树
     * @param busiId
     */
    void deleteByBusiId(Long busiId);

    /**
     * 批量插入动态树表
     * @param busiDocListTemp
     */
    void saveList(List<EcmBusiDoc> busiDocListTemp);

    /**
     * 查询对应标记
     * 根据busiId和docCode和markName查询 标记
     * @return
     */
    EcmBusiDoc selectMark(Long busiId, String docCode, String markName);
}
