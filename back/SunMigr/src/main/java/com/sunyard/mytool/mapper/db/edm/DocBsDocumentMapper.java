package com.sunyard.mytool.mapper.db.edm;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.mytool.entity.DocBsDocument;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("EDMDataSource")
public interface DocBsDocumentMapper extends BaseMapper<DocBsDocument> {
}
