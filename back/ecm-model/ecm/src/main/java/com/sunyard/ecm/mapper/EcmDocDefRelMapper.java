package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.dto.ecm.EcmDocDefDTO;
import com.sunyard.ecm.po.EcmDocDefRel;

import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 业务资料关联接口
 */
public interface EcmDocDefRelMapper extends BaseMapper<EcmDocDefRel> {
    /**
     * @return
     */
    List<EcmDocDefDTO> selectAllChildrenInfo();
}
