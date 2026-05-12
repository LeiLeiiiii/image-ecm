package com.sunyard.mytool.service.edm;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.DocTemp;

import java.util.List;

public interface DocTempService extends IService<DocTemp> {

    /**
     * 查询待迁移数据
     */
    Page<DocTemp> selectMigData();

    /**
     * 批量更新状态
     */
    void updateBatchStatus(List<DocTemp> recordsList, int status);

}
