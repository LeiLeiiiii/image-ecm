package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmSysLabel;
import com.sunyard.mytool.mapper.db.ecm.EcmSysLabelMapper;
import com.sunyard.mytool.service.ecm.EcmSysLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EcmSysLabelServiceImpl extends ServiceImpl<EcmSysLabelMapper, EcmSysLabel> implements EcmSysLabelService {
    @Autowired
    private EcmSysLabelMapper ecmSysLabelMapper;
    @Override
    public EcmSysLabel selectByLabelName(String labelName) {
        LambdaQueryWrapper<EcmSysLabel> qw = new LambdaQueryWrapper<>();
        qw.eq(EcmSysLabel::getLabelName,labelName);
        return ecmSysLabelMapper.selectOne(qw);
    }

    @Override
    public List<EcmSysLabel> findAll() {
        return list();
    }
}
