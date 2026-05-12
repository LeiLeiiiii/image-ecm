package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.EcmBusiInfo;


public interface EcmBusiInfoService extends IService<EcmBusiInfo> {

    /**
     * 新增ecmBusiInfo
     */
     void insertBusiInfo(EcmBusiInfo ecmBusiInfo);


    /**
     * 修改ecmBusiInfo
     */
    void updateBusiInfo(EcmBusiInfo ecmBusiInfo);


    /**
     * 根据appCode和busiNo获取业务信息
     */
    EcmBusiInfo getByAppCodeAndBusiNo(String appCode, String busiNo);
}
