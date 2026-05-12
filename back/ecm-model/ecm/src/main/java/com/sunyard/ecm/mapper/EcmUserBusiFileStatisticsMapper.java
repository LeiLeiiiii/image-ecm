package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.po.EcmUserBusiFileStatistics;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 */
public interface EcmUserBusiFileStatisticsMapper extends BaseMapper<EcmUserBusiFileStatistics> {
    /**
     *
     * @param ecmStatisticsDTO
     */
    void insertBatch(@Param("list") List<EcmUserBusiFileStatistics> ecmStatisticsDTO);
}
