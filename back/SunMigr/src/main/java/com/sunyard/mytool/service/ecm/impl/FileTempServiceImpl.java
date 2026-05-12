package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.ecm.EcmFileInfo;
import com.sunyard.mytool.entity.ecm.FileTemp;
import com.sunyard.mytool.mapper.db.ecm.EcmFileInfoMapper;
import com.sunyard.mytool.mapper.db.ecm.FileTempMapper;
import com.sunyard.mytool.service.ecm.FileTempService;
import com.sunyard.mytool.until.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FileTempServiceImpl extends ServiceImpl<FileTempMapper, FileTemp> implements FileTempService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 查询批次下待迁移文件
     */
    @Override
    public List<FileTemp> listByAppCodeAndBusiNo(String appCode, String busiNo) {
        if (StringUtils.isBlank(busiNo)) {
            throw new RuntimeException("busiNo为空");
        }
        if (StringUtils.isBlank(appCode)) {
            throw new RuntimeException("appCode为空");
        }
        try {
            LambdaQueryWrapper<FileTemp> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FileTemp::getBusiNo, busiNo);
            queryWrapper.eq(FileTemp::getAppCode, appCode);
            queryWrapper.eq(FileTemp::getMigStatus, 0);
            log.debug("开始查询文件表");
            return baseMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("查询文件中间表数据时发生异常busiNo:{},appCode:{}", busiNo, appCode, e);
            throw new RuntimeException("查询文件中间表数据时发生异常");
        }
    }

    /**
     * 设置迁移失败文件状态
     */
    @DS("ECMDataSource")
    @Override
    @DSTransactional
    public void saveOrUpdateFileTemp(FileTemp fileTemp) {
        String failReason = fileTemp.getFailReason();
        if (StringUtils.isNotBlank(failReason) && failReason.length() > 1024) {
            failReason = failReason.substring(0, 1024) + "(后略)";
        }
        fileTemp.setFailReason(failReason);
        saveOrUpdate(fileTemp);
    }

    /**
     * 根据appcode和busino修改迁移状态
     */
    @DS("ECMDataSource")
    @Override
    @DSTransactional
    public void updateMigStatusByAppCodeAndBusiNo(String appCode, String busiNo, Integer migStatus,String failReason) {
        LambdaUpdateWrapper<FileTemp> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FileTemp::getBusiNo, busiNo);
        updateWrapper.eq(FileTemp::getAppCode, appCode);
        updateWrapper.ne(FileTemp::getMigStatus, 2);
        updateWrapper.set(FileTemp::getMigStatus, migStatus);
        if (StringUtils.isNotBlank(failReason) && failReason.length() > 1024) {
            failReason = failReason.substring(0, 1024) + "(后略)";
        }
        updateWrapper.set(FileTemp::getFailReason, failReason);
        baseMapper.update(null, updateWrapper);
    }

    /**
     * 根据id批量修改迁移状态
     */
    @DS("ECMDataSource")
    @Override
    @DSTransactional
    public void updateMigStatusByIds(List<String> ids, Integer status) {
        if (CollectionUtils.isEmpty(ids)) {
            return; // 空集合直接返回，避免无效操作
        }
        MybatisBatch<String> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, ids);
        MybatisBatch.Method<FileTemp> method = new MybatisBatch.Method<>(FileTempMapper.class);
        mybatisBatch.execute(method.update(id ->
                new LambdaUpdateWrapper<FileTemp>()
                        .eq(FileTemp::getFileId, id)
                        .set(FileTemp::getMigStatus, status)
                        .set(FileTemp::getFailReason, "")
                        .set(FileTemp::getMigTime, new Date())
        ));

    }

    /**
     * 根据id修改迁移状态
     */
    @DS("ECMDataSource")
    @Override
    @DSTransactional
    public void updateMigStatusById(String fileId, Integer status,String failReason) {
        LambdaUpdateWrapper<FileTemp> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FileTemp::getFileId, fileId);
        updateWrapper.set(FileTemp::getMigStatus, status);
        updateWrapper.set(FileTemp::getMigTime, new Date());
        if (StringUtils.isNotBlank(failReason) && failReason.length() > 1024) {
            failReason = failReason.substring(0, 1024) + "(后略)";
        }
        updateWrapper.set(FileTemp::getFailReason, failReason);
        baseMapper.update(null, updateWrapper);
    }

    @Override
    public List<FileTemp> selectBatch(String appCode, String busiNo) {
        LambdaQueryWrapper<FileTemp> qw = new LambdaQueryWrapper<>();
        qw.eq(FileTemp::getAppCode, appCode);
        qw.eq(FileTemp::getBusiNo, busiNo);
        return baseMapper.selectList(qw);
    }

    @DS("ECMDataSource")
    @Override
    @DSTransactional
    public void updateSuccessBatch(List<FileTemp> fileTemps) {
        if (CollectionUtils.isEmpty(fileTemps)){
            return;
        }
        MybatisBatch<FileTemp> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, fileTemps);
        MybatisBatch.Method<FileTemp> method = new MybatisBatch.Method<>(FileTempMapper.class);
        mybatisBatch.execute(method.updateById());
    }

    @DS("ECMDataSource")
    @Override
    @DSTransactional
    public void saveBatch(List<FileTemp> fileTemps) {
        if (CollectionUtils.isEmpty(fileTemps)){
            return;
        }
        MybatisBatch<FileTemp> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, fileTemps);
        MybatisBatch.Method<FileTemp> method = new MybatisBatch.Method<>(FileTempMapper.class);
        mybatisBatch.execute(method.insert());
    }

}
