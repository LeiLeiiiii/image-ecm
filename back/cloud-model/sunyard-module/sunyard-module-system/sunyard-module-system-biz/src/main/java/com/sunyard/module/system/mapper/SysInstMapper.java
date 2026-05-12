package com.sunyard.module.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.api.dto.SysInstExportDTO;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.po.SysInst;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhouleibin
 * @since 2021-08-16
 */
public interface SysInstMapper extends BaseMapper<SysInst> {
    /**
     * 查询机构树
     * 
     * @param parentId 父级id
     * @return Result
     */
    List<SysOrgDTO> searchInstTree(@Param("parentId") Long parentId);

    /**
     * 导出机构列表
     *
     * @return Result
     */
    List<SysInstExportDTO> exportList();
}
