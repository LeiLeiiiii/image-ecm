package com.sunyard.module.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.api.dto.SysDeptExportDTO;
import com.sunyard.module.system.api.dto.SysOrgDTO;
import com.sunyard.module.system.po.SysDept;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhouleibin
 * @since 2021-08-16
 */
public interface SysDeptMapper extends BaseMapper<SysDept> {
    /**
     * 查询部门树
     *
     * @param parentId 父级id
     * @return Result
     */
    List<SysOrgDTO> searchDeptTree(@Param("parentId") Long parentId);

    /**
     * 部门导出列表
     * @param parentId 父级id
     * @return Result
     */
    List<SysDeptExportDTO> exportListByInstId(@Param("parentId") Long parentId);

    /**
     * 获取所有父级
     * @param folderDept 父级部门
     * @return Result
     */
    List<SysDept> selectAllFather(@Param("folderDept") Long folderDept);
}
