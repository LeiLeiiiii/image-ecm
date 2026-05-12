package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.FileTemp;

import java.util.ArrayList;
import java.util.List;

public interface FileTempService extends IService<FileTemp> {


    /**
     * 查询批次下待迁移文件
     */
    List<FileTemp> listByAppCodeAndBusiNo(String appCode, String busiNo);

    /**
     * 设置迁移失败文件状态
     */
    void saveOrUpdateFileTemp(FileTemp fileTemp);

    /**
     * 根据appcode+busino批量修改迁移状态
     */
    void updateMigStatusByAppCodeAndBusiNo(String appCode, String busiNo, Integer migStatus,String failReason);

    /**
     * 根据id批量修改迁移状态
     */
    void updateMigStatusByIds(List<String> ids, Integer status);

    /**
     * 根据id修改迁移状态
     */
    void updateMigStatusById(String fileId ,Integer status,String failReason);

    /**
     * 查询批次下所有文件
     */
    List<FileTemp> selectBatch(String appCode, String busiNo);

    /**
     * 批量修改
     */
    void updateSuccessBatch(List<FileTemp> fileTemps);

    /**
     * 批量插入
     */
    void saveBatch(List<FileTemp> fileTemps);
}
