package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.EcmSysLabel;

import java.util.List;

public interface EcmSysLabelService extends IService<EcmSysLabel> {

    /**
     * 根据标签名称查询标签
     */
    EcmSysLabel selectByLabelName(String labelName);

    /**
     * 查询所有系统标签
     */
    List<EcmSysLabel> findAll();

}
