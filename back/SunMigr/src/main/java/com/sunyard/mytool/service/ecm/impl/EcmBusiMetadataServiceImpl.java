package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmBusiMetadata;
import com.sunyard.mytool.mapper.db.ecm.EcmBusiMetadataMapper;
import com.sunyard.mytool.service.ecm.EcmBusiMetadataService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EcmBusiMetadataServiceImpl extends ServiceImpl<EcmBusiMetadataMapper, EcmBusiMetadata> implements EcmBusiMetadataService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Override
    public EcmBusiMetadata selectByBusiIdAndAppAttrId(Long busiId, Long appAttrId) {
        LambdaQueryWrapper<EcmBusiMetadata> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmBusiMetadata::getBusiId, busiId);
        queryWrapper.eq(EcmBusiMetadata::getAppAttrId, appAttrId);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    @DS("ECMDataSource")
    public void saveOrUpdateMetada(List<EcmBusiMetadata> metaDataListToInsert, List<EcmBusiMetadata> metaDataListToUpdate) {
        if (!metaDataListToInsert.isEmpty()){
            MybatisBatch<EcmBusiMetadata> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, metaDataListToInsert);
            MybatisBatch.Method<EcmBusiMetadata> method = new MybatisBatch.Method<>(EcmBusiMetadataMapper.class);
            mybatisBatch.execute(method.insert());
        }
        if (!metaDataListToUpdate.isEmpty()){
            MybatisBatch<EcmBusiMetadata> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, metaDataListToUpdate);
            MybatisBatch.Method<EcmBusiMetadata> method = new MybatisBatch.Method<>(EcmBusiMetadataMapper.class);
            mybatisBatch.execute(method.updateById());
        }

    }
}
