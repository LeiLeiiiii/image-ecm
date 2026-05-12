package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmBusiDocRel;
import com.sunyard.mytool.mapper.db.ecm.EcmBusiDocRelMapper;
import com.sunyard.mytool.service.ecm.EcmBusiDocRelService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EcmBusiDocRelServiceImpl extends ServiceImpl<EcmBusiDocRelMapper, EcmBusiDocRel> implements EcmBusiDocRelService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @DS("ECMDataSource")
    @Override
    @Transactional
    public void deleteByDocIds(List<Long> docIdList) {
        LambdaQueryWrapper<EcmBusiDocRel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(EcmBusiDocRel::getDocId, docIdList);
        baseMapper.delete(queryWrapper);
    }

    @DS("ECMDataSource")
    @Override
    @Transactional
    public void saveList(List<EcmBusiDocRel> docRelList) {
        MybatisBatch<EcmBusiDocRel> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, docRelList);
        MybatisBatch.Method<EcmBusiDocRel> method = new MybatisBatch.Method<>(EcmBusiDocRelMapper.class);
        mybatisBatch.execute(method.insert());
    }
}