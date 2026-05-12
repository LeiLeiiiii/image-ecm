package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.dto.ecm.EcmFileAutoDTO;
import com.sunyard.ecm.po.EcmBusiDoc;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 业务资料接口
 */
public interface EcmBusiDocMapper extends BaseMapper<EcmBusiDoc> {
    /**
     *
     * @param busiId
     * @return
     */
    List<EcmFileAutoDTO> selectDtdCode(@Param("busiId") Long busiId);

    /**
     *
     * @param appCode
     * @return
     */
    List<EcmFileAutoDTO> staticTreeselectDtdCode(@Param("appCode") String appCode);
}
