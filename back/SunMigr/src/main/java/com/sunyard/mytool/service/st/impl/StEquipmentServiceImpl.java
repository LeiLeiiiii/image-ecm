package com.sunyard.mytool.service.st.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.StEquipment;
import com.sunyard.mytool.mapper.db.st.StEquipmentMapper;
import com.sunyard.mytool.service.st.StEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class StEquipmentServiceImpl extends ServiceImpl<StEquipmentMapper, StEquipment> implements StEquipmentService {

    @Autowired
    private  StEquipmentMapper stEquipmentMapper;


    @Override
    public StEquipment findById(Long Id) {
        return stEquipmentMapper.selectById(Id);
    }

    @Override
    public List<StEquipment> findAll() {
        return list();
    }

    @Override
    public StEquipment findByBucket(String bucket) {
        LambdaQueryWrapper<StEquipment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StEquipment::getBucket, bucket);
        return baseMapper.selectOne(queryWrapper);
    }
}
