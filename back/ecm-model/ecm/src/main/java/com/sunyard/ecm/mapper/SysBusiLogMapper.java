package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.po.EcmBusiLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;

/**
 * @author ty
 * @since 2023-8-01 9:43
 * @desc 业务日志接口
 */
@Mapper
public interface SysBusiLogMapper extends BaseMapper<EcmBusiLog> {

    /**
     * 批量插入
     *
     * @param busiLogColl
     * @return
     */
    int insertBatch(@Param("coll")Collection busiLogColl);

}
