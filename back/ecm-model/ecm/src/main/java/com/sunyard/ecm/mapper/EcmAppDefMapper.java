package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.po.EcmAppDef;
import com.sunyard.ecm.vo.EcmBusiStorageListVO;

import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 业务类型定义接口
 */
public interface EcmAppDefMapper extends BaseMapper<EcmAppDef> {
    /**
     * 查询最后一层级数据
     * @param
     * @return
     */
    List<EcmBusiStorageListVO> selectLastList();
}
