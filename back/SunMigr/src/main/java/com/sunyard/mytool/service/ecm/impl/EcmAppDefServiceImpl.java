package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmAppDef;
import com.sunyard.mytool.mapper.db.ecm.EcmAppDefMapper;
import com.sunyard.mytool.service.ecm.EcmAppDefService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class EcmAppDefServiceImpl extends ServiceImpl<EcmAppDefMapper, EcmAppDef> implements EcmAppDefService {
    @Override
    public List<EcmAppDef> findAll() {
        return list();
    }
}
