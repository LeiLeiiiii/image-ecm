package com.sunyard.mytool.service.ecm;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sunyard.mytool.entity.ecm.EcmDocDef;


import java.util.List;

public interface EcmDocDefService extends IService<EcmDocDef> {
    List<EcmDocDef> findAll();
}
