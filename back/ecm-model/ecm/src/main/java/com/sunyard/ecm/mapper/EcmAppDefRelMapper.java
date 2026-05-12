package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.po.EcmAppDefRel;
import com.sunyard.ecm.vo.EcmAppDefAttrVO;

import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 业务关联接口
 */
public interface EcmAppDefRelMapper extends BaseMapper<EcmAppDefRel> {
    /**
     * @return
     */
    List<EcmAppDefAttrVO> searchAllAppDefRelInfo();
}
