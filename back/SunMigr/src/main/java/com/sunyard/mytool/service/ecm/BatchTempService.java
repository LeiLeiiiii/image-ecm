package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.BatchTemp;


import java.util.List;


public interface BatchTempService extends IService<BatchTemp> {

    /**
     * 根据id查询
     */
    BatchTemp getByPK(long id);

    /**
     * 根据id更新状态
     */
    void updateOneMigrateBatchStatus(long id, int status, String failReason);

    /**
     * 批量更新状态
     * @param resultList
     */
    void updateBatchStatus(List<BatchTemp> resultList,Integer status);

    /**
     * 分页查询待迁移数据
     * @return
     */
    Page<BatchTemp> selectMigData();

}
