package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmFileInfo;
import com.sunyard.mytool.mapper.db.ecm.EcmFileInfoMapper;
import com.sunyard.mytool.service.ecm.EcmFileInfoService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class EcmFileInfoServiceImpl extends ServiceImpl<EcmFileInfoMapper, EcmFileInfo> implements EcmFileInfoService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 根据busiId查询EcmFileInfo列表
     */
    @Override
    public List<EcmFileInfo> getByBusiId(Long busiId) {
        LambdaQueryWrapper<EcmFileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmFileInfo::getBusiId, busiId)
                .isNotNull(EcmFileInfo::getPageId);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 批量插入EcmFileInfo
     */
    @DS("ECMDataSource")
    @DSTransactional
    @Override
    public void saveList(List<EcmFileInfo> ecmFileInfos) {
        if (ecmFileInfos == null || ecmFileInfos.isEmpty()) {
            return;
        }
        MybatisBatch<EcmFileInfo> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ecmFileInfos);
        MybatisBatch.Method<EcmFileInfo> method = new MybatisBatch.Method<>(EcmFileInfoMapper.class);
        mybatisBatch.execute(method.insert());
    }

    /**
     * 批量删除EcmFileInfo
     */
    @DS("ECMDataSource")
    @DSTransactional
    @Override
    public void deleteByIds(List<Long> ecmFileIdsToDelete) {
        baseMapper.deleteBatchIds(ecmFileIdsToDelete);
    }

}