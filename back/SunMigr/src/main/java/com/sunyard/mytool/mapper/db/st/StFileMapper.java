package com.sunyard.mytool.mapper.db.st;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.mytool.entity.StFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("storageDataSource")
public interface StFileMapper extends BaseMapper<StFile> {
}
