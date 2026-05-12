package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.EcmAppAttr;

import java.util.List;

public interface EcmAppAttrService extends IService<EcmAppAttr> {
    List<EcmAppAttr> findAll();

    List<EcmAppAttr> getByAppCode(String appCode);
}
