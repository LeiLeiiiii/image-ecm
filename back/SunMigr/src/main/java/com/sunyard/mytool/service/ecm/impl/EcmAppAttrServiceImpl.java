package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmAppAttr;
import com.sunyard.mytool.mapper.db.ecm.EcmAppAttrMapper;
import com.sunyard.mytool.service.ecm.EcmAppAttrService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class EcmAppAttrServiceImpl extends ServiceImpl<EcmAppAttrMapper, EcmAppAttr> implements EcmAppAttrService {
    @Override
    public List<EcmAppAttr> findAll() {
        return list();
    }

    @Override
    public List<EcmAppAttr> getByAppCode(String appCode) {
        LambdaQueryWrapper<EcmAppAttr> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmAppAttr::getAppCode, appCode);
        return baseMapper.selectList(queryWrapper);
    }
}
