package com.sunyard.mytool.mapper.db.edm;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.mytool.entity.DocTemp;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("EDMDataSource")
public interface DocTempMapper extends BaseMapper<DocTemp> {
}
