package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.EcmFileInfo;

import java.util.List;

public interface EcmFileInfoService extends IService<EcmFileInfo> {


    /**
     * 根据busiId查询EcmFileInfo列表
     */
    List<EcmFileInfo> getByBusiId(Long busiId);

    /**
     * 批量插入EcmFileInfo
     */
    void saveList(List<EcmFileInfo> ecmFileInfos);

    /**
     * 批量删除EcmFileInfo
     */
    void deleteByIds(List<Long> ecmFileIdsToDelete);
}
