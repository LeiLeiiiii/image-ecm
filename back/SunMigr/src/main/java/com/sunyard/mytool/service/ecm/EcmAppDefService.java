package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.EcmAppDef;

import java.util.List;

public interface EcmAppDefService extends IService<EcmAppDef> {
    List<EcmAppDef> findAll();
}
