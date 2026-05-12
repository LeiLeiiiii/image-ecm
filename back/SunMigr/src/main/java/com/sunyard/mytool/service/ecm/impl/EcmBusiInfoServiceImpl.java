package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmBusiInfo;
import com.sunyard.mytool.mapper.db.ecm.EcmBusiInfoMapper;
import com.sunyard.mytool.service.ecm.EcmBusiInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EcmBusiInfoServiceImpl extends ServiceImpl<EcmBusiInfoMapper, EcmBusiInfo> implements EcmBusiInfoService {

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 修改ecmBusiInfo
     */
    @DS("ECMDataSource")
    @Override
    @DSTransactional
    public void updateBusiInfo(EcmBusiInfo ecmBusiInfo) {
        try {
            LambdaUpdateWrapper<EcmBusiInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(EcmBusiInfo::getBusiId, ecmBusiInfo.getBusiId());
            baseMapper.update(ecmBusiInfo, updateWrapper);
        } catch (Exception e) {
            logger.error("业务信息表(ECM_BUSI_INFO)修改失败，busiNo: {}, appCode: {}", ecmBusiInfo.getBusiNo(), ecmBusiInfo.getAppCode(), e);
            throw new RuntimeException("业务信息表(ECM_BUSI_INFO)存储失败");
        }
    }

    /**
     * 新增ecmBusiInfo
     */
    @DS("ECMDataSource")
    @Override
    @DSTransactional
    public void insertBusiInfo(EcmBusiInfo ecmBusiInfo) {
        try {
            baseMapper.insert(ecmBusiInfo);
        } catch (Exception e) {
            logger.error("业务信息表(ECM_BUSI_INFO)存储失败，busiNo: {}, appCode: {}", ecmBusiInfo.getBusiNo(), ecmBusiInfo.getAppCode(), e);
            throw new RuntimeException("业务信息表(ECM_BUSI_INFO)存储失败");
        }
    }


    /**
     * 根据appCode和busiNo获取业务信息
     */
    @Override
    public EcmBusiInfo getByAppCodeAndBusiNo(String appCode, String busiNo) {
        LambdaQueryWrapper<EcmBusiInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EcmBusiInfo::getBusiNo, busiNo)
                .eq(EcmBusiInfo::getAppCode, appCode);
        EcmBusiInfo EcmBusiInfo = baseMapper.selectOne(queryWrapper);
        return EcmBusiInfo;
    }
}
