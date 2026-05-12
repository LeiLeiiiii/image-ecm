package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.dto.ecm.EcmBusiRecycleInfoDTO;
import com.sunyard.ecm.po.EcmBusiRecycle;
import com.sunyard.ecm.vo.EcmBusiRecycleSearchVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务回收mapper
 *
 * @author wzz
 * @since 2024-6-6
 */
public interface EcmBusiRecycleMapper extends BaseMapper<EcmBusiRecycle> {

    /**
     * 批量插入
     *
     * @param ecmBusiRecycleList
     * @return
     */
    int insertBatch(@Param("list") List<EcmBusiRecycle> ecmBusiRecycleList);

    /**
     * 回收业务数据查询
     *
     * @param searchVO
     * @param appCodeList
     * @param busiIds
     * @return
     */
    List<EcmBusiRecycleInfoDTO> selectBusiRecycleList(@Param("searchVO") EcmBusiRecycleSearchVO searchVO,
                                                      @Param("appCodeList") List<String> appCodeList,
                                                      @Param("busiIds") List<Long> busiIds);


}
