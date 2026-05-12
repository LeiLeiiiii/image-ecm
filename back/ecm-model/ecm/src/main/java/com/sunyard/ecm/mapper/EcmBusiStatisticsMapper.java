package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.dto.ecm.statistics.EcmBusiStatisticsDTO;
import com.sunyard.ecm.po.EcmBusiStatistics;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;

/**
 * @author zsc
 * @date 2024-06-17
 */
public interface EcmBusiStatisticsMapper extends BaseMapper<EcmBusiStatistics> {

    /**
     *
     */
    void insertBatch(@Param("list") List<EcmBusiStatistics> ecmBusiStatisticsList);

    /**
     *
     */
    void updateBatch(@Param("list") List<EcmBusiStatistics> ecmBusiStatisticsList);

    /**
     *
     */
    EcmBusiStatisticsDTO selectCounts();

    /**
     *
     */
    List<EcmBusiStatisticsDTO> selectCountsDay(@Param("orgCodeList") List<String> orgCodeList,
                                               @Param("appCodeList") List<String> appCodeList,
                                               @Param("createTimeStart") Date createTimeStart,
                                               @Param("createTimeEnd") Date createTimeEnd);

    /**
     *
     */
    List<EcmBusiStatisticsDTO> selectCountsMoon(@Param("orgCodeList") List<String> orgCodeList,
                                                @Param("appCodeList") List<String> appCodeList,
                                                @Param("createTimeStart") Date createTimeStart,
                                                @Param("createTimeEnd") Date createTimeEnd);

    /**
     *
     */
    List<EcmBusiStatisticsDTO> selectCountsYear(@Param("orgCodeList") List<String> orgCodeList,
                                                @Param("appCodeList") List<String> appCodeList,
                                                @Param("createTimeStart") Date createTimeStart,
                                                @Param("createTimeEnd") Date createTimeEnd);

    /**
     *
     */
    List<EcmBusiStatisticsDTO> selectEcmBusiStatisticsDTOList(@Param("orgCodeList") List<String> orgCodeList,
                                                              @Param("appCodeList") List<String> appCodeList,
                                                              @Param("createTimeStart") Date createTimeStart,
                                                              @Param("createTimeEnd") Date createTimeEnd,
                                                              @Param("sortColumn") String sortColumn,
                                                              @Param("sortRule") String sortRule);

    /**
     *
     */
    List<EcmBusiStatisticsDTO> trafficPie(@Param("orgCodeList") List<String> orgCodeList,
                                          @Param("appCodeList") List<String> appCodeList,
                                          @Param("createTimeStart") Date createTimeStart,
                                          @Param("createTimeEnd") Date createTimeEnd);
}
