package com.sunyard.sunafm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.sunafm.po.AfmApiData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 对外接口请求数据 Mapper 接口
 * </p>
 *
 * @author pjw
 * @since 2024-04-11
 */
public interface AfmApiDataMapper extends BaseMapper<AfmApiData> {
    void insertBatch(@Param("list") List<AfmApiData> dtos1);
}
