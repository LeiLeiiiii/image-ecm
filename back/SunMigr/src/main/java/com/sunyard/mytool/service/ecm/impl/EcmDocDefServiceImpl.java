package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmDocDef;
import com.sunyard.mytool.mapper.db.ecm.EcmDocDefMapper;
import com.sunyard.mytool.service.ecm.EcmDocDefService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EcmDocDefServiceImpl extends ServiceImpl<EcmDocDefMapper, EcmDocDef> implements EcmDocDefService {
    @Override
    public List<EcmDocDef> findAll() {
        return list();
    }
}
