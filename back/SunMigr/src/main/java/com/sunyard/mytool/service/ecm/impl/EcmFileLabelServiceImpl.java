package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmFileLabel;
import com.sunyard.mytool.mapper.db.ecm.EcmFileLabelMapper;
import com.sunyard.mytool.service.ecm.EcmFileLabelService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;

@Service
public class EcmFileLabelServiceImpl extends ServiceImpl<EcmFileLabelMapper, EcmFileLabel> implements EcmFileLabelService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @DS("ECMDataSource")
    @Override
    public void saveList(ArrayList<EcmFileLabel> ecmFileLabels) {
        if (!ecmFileLabels.isEmpty()) {
            MybatisBatch<EcmFileLabel> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmFileLabels);
            MybatisBatch.Method<EcmFileLabel> method = new MybatisBatch.Method<>(EcmFileLabelMapper.class);
            mybatisBatch.execute(method.insert());
        }
    }
}
