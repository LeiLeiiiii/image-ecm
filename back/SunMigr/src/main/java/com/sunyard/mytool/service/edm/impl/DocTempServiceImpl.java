package com.sunyard.mytool.service.edm.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.constant.MigrateConstant;
import com.sunyard.mytool.entity.DocTemp;
import com.sunyard.mytool.mapper.db.edm.DocTempMapper;
import com.sunyard.mytool.service.edm.DocTempService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@DS("EDMDataSource")
public class DocTempServiceImpl extends ServiceImpl<DocTempMapper, DocTemp> implements DocTempService {

    @Autowired
    private DocTempMapper docTempMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 查询带迁移数据 分页大小50
     */
    @Override
    public Page<DocTemp> selectMigData() {
        LambdaQueryWrapper<DocTemp> lambdaQuery = new LambdaQueryWrapper<>();
            lambdaQuery.eq(DocTemp::getMigStatus, MigrateConstant.MIGRATE_WAITING);
            //分页获取前多少条数据
            Page<DocTemp> page = new Page<>(1, 50,false);
            return docTempMapper.selectPage(page, lambdaQuery);
    }

    /**
     * 更新迁移状态
     */
    @Override
    @Transactional
    public void updateBatchStatus(List<DocTemp> recordsList, int status) {
        MybatisBatch<DocTemp> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, recordsList);
        MybatisBatch.Method<DocTemp> method = new MybatisBatch.Method<>(DocTempMapper.class);
        mybatisBatch.execute(method.update(entity ->
                new LambdaUpdateWrapper<DocTemp>()
                        .set(DocTemp::getMigStatus, status)
                        .eq(DocTemp::getPkId, entity.getPkId())

        ));
    }
}
