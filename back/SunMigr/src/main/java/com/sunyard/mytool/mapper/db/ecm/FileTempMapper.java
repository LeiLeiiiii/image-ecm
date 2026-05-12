package com.sunyard.mytool.mapper.db.ecm;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.mytool.entity.ecm.FileTemp;
import org.apache.ibatis.annotations.Mapper;

@DS("ECMDataSource")
@Mapper
public interface FileTempMapper extends BaseMapper<FileTemp> {

}
