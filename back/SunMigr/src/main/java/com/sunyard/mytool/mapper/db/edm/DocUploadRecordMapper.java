package com.sunyard.mytool.mapper.db.edm;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.mytool.entity.DocUploadRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("EDMDataSource")
public interface DocUploadRecordMapper extends BaseMapper<DocUploadRecord> {
}
