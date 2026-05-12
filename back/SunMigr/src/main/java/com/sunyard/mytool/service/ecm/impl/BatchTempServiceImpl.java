package com.sunyard.mytool.service.ecm.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.constant.MigrateConstant;
import com.sunyard.mytool.entity.ecm.BatchTemp;
import com.sunyard.mytool.mapper.db.ecm.BatchTempMapper;
import com.sunyard.mytool.service.ecm.BatchTempService;
import com.sunyard.mytool.until.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 迁移数据表管理服务
 */
@Service
@DS("ECMDataSource")
public class BatchTempServiceImpl extends ServiceImpl<BatchTempMapper, BatchTemp> implements BatchTempService {


    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private BatchTempMapper batchTempMapper;

    /**
     * 根据id查询
     */
    @Override
    public BatchTemp getByPK(long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 根据id更新状态
     */
    @Override
    @DSTransactional
    @DS("ECMDataSource")
    public void updateOneMigrateBatchStatus(long id, int status, String failReason) {
        long start = System.currentTimeMillis();
        try {
            UpdateWrapper<BatchTemp> wrapper = new UpdateWrapper<BatchTemp>();
            BatchTemp updateData = new BatchTemp();
            updateData.setMigStatus(status);
            if (StringUtils.isNotBlank(failReason) && failReason.length() > 1024) {
                failReason = failReason.substring(0, 1024) + "(后略)";
            }
            updateData.setFailReason(failReason);
            updateData.setMigTime(new Date());
            wrapper.eq("id", id);
            baseMapper.update(updateData, wrapper);
        } finally {
            logger.debug("*更新迁移状态*耗时:{}", System.currentTimeMillis() - start);
        }
    }

    /**
     * 批量更新状态
     * @param resultList
     */
    @Override
    @DSTransactional
    @DS("ECMDataSource")
    public void updateBatchStatus(List<BatchTemp> resultList,Integer status) {
        MybatisBatch<BatchTemp> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, resultList);
        MybatisBatch.Method<BatchTemp> method = new MybatisBatch.Method<>(BatchTempMapper.class);
        mybatisBatch.execute(method.update(entity ->
                new LambdaUpdateWrapper<BatchTemp>()
                        .set(BatchTemp::getMigStatus, status)
                        .eq(BatchTemp::getId, entity.getId())

        ));
    }

    /**
     * 查询带迁移数据 分页大小50
     */
    @Override
    public Page<BatchTemp> selectMigData() {
        LambdaQueryWrapper<BatchTemp> lambdaQuery = new LambdaQueryWrapper<>();
        lambdaQuery.eq(BatchTemp::getMigStatus, MigrateConstant.MIGRATE_WAITING);
        //分页获取前多少条数据
        Page<BatchTemp> page = new Page<>(1, 50,false);
        return batchTempMapper.selectPage(page, lambdaQuery);
    }

}
