package com.sunyard.mytool.service.st;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.StEquipment;

import java.util.List;

public interface StEquipmentService extends IService<StEquipment> {

    /**
     * 根据Id查询设备信息
     */
    StEquipment findById(Long Id);

    List<StEquipment> findAll();


    StEquipment findByBucket(String bucket);
}
