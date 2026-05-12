package com.sunyard.ecm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.ecm.po.EcmDocDefRelVer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ty
 * @since 2023-4-18 9:43
 * @desc 业务资料关联版本接口
 */
public interface EcmDocDefRelVerMapper  extends BaseMapper<EcmDocDefRelVer> {
    /**
     * 批量插入
     * @param list
     */
    void batchInsert(@Param("list") List<EcmDocDefRelVer> list);
}
