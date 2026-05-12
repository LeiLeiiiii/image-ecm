package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmBusiDoc;
import com.sunyard.mytool.mapper.db.ecm.EcmBusiDocMapper;
import com.sunyard.mytool.service.ecm.EcmBusiDocService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EcmBusiDocServiceImpl extends ServiceImpl<EcmBusiDocMapper, EcmBusiDoc> implements EcmBusiDocService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private EcmBusiDocMapper ecmBusiDocMapper;
    @Override
    public List<EcmBusiDoc> getByBusiId(Long busiId) {
        LambdaQueryWrapper<EcmBusiDoc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmBusiDoc::getBusiId, busiId);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    @DSTransactional
    @DS("ECMDataSource")
    public void deleteByBusiId(Long busiId) {
        LambdaQueryWrapper<EcmBusiDoc> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmBusiDoc::getBusiId, busiId);
        baseMapper.delete(queryWrapper);
    }

    @Override
    @DSTransactional
    @DS("ECMDataSource")
    public void saveList(List<EcmBusiDoc> busiDocListTemp) {
        if (busiDocListTemp == null || busiDocListTemp.isEmpty()) {
            return;
        }
        MybatisBatch<EcmBusiDoc> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, busiDocListTemp);
        MybatisBatch.Method<EcmBusiDoc> method = new MybatisBatch.Method<>(EcmBusiDocMapper.class);
        mybatisBatch.execute(method.insert());
    }

    @Override
    public EcmBusiDoc selectMark(Long busiId, String docCode, String markName) {
        LambdaQueryWrapper<EcmBusiDoc> qw = new LambdaQueryWrapper<>();
        qw.eq(EcmBusiDoc::getBusiId, busiId)
                .eq(EcmBusiDoc::getDocCode, docCode)
                .eq(EcmBusiDoc::getDocName, markName);
        return baseMapper.selectOne(qw);
    }
}
