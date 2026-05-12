package com.sunyard.mytool.service.st.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sunyard.mytool.entity.StFile;
import com.sunyard.mytool.mapper.db.st.StFileMapper;
import com.sunyard.mytool.service.st.StFileService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;

@Service
public class StFileServiceImpl extends ServiceImpl<StFileMapper, StFile> implements StFileService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;


    /**
     * 批量保存
     */
    @DS("storageDataSource")
    @DSTransactional
    @Override
    public void saveList(ArrayList<StFile> stFiles) {
        if (stFiles == null || stFiles.isEmpty()) {
            return;
        }
        MybatisBatch<StFile> mybatisBatch = new MybatisBatch<>(sqlSessionFactory, stFiles);
        MybatisBatch.Method<StFile> method = new MybatisBatch.Method<>(StFileMapper.class);
        mybatisBatch.execute(method.insert());
    }

}
