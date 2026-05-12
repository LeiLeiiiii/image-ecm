package com.sunyard.mytool.mapper.db.ecm;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.mytool.entity.ecm.EcmFileLabel;
import org.apache.ibatis.annotations.Mapper;

@DS("ECMDataSource")
@Mapper
public interface EcmFileLabelMapper extends BaseMapper<EcmFileLabel> {
}
