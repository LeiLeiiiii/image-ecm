package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.po.EcmBusiMetadata;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 业务属性值接口
 */
public interface EcmBusiMetadataMapper extends BaseMapper<EcmBusiMetadata> {
    /**
     * 业务属性混合查询 todo
     *
     * @param filterAttr
     * @param size
     * @param appTypeIds
     */
    List<Long> complexSelect(@Param("list") List<EcmAppAttrDTO> filterAttr, @Param("size") Integer size,
                             @Param("appTypeIds") List<String> appTypeIds);
}
