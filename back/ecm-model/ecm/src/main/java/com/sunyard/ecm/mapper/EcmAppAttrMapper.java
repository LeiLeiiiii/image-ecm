package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.po.EcmAppAttr;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 业务属性接口
 */
public interface EcmAppAttrMapper extends BaseMapper<EcmAppAttr> {
    /**
     * @param list
     * @return
     */
    int insertList(@Param("list") List list);
}
