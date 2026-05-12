package com.sunyard.sunafm.mapper;

import com.sunyard.sunafm.po.AfmImageDupAssoc;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 图像查重关联表; Mapper 接口
 * </p>
 *
 * @author pjw
 * @since 2024-04-07
 */
public interface AfmImageDupAssocMapper extends BaseMapper<AfmImageDupAssoc> {
    void insertBatch(@Param("list") List<AfmImageDupAssoc> addlist);
}
