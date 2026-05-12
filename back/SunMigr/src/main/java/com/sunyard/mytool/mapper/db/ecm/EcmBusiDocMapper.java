package com.sunyard.mytool.mapper.db.ecm;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.mytool.entity.ecm.EcmBusiDoc;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@DS("ECMDataSource")
@Mapper
public interface EcmBusiDocMapper extends BaseMapper<EcmBusiDoc> {
    void saveList(List<EcmBusiDoc> busiDocListTemp);
}
