package com.sunyard.module.system.mapper;

import java.util.List;

import com.sunyard.module.system.api.dto.SysRoleDTO;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.po.SysRole;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhouleibin
 * @since 2021-08-16
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 根据用户查询角色
     * 
     * @param userId 用户id
     * @return Result
     */
    List<SysRole> searchLinkedRole(@Param("userId") Long userId);

    /**
     * 根据用户id查重角色信息按“，”分割
     * @param userId 用户id
     * @return Result
     */
    List<SysRole> searchRoleNameByUserId(@Param("userId") Long userId);


    /**
     * 查询角色信息
     * @param sysRoleDTO dto
     * @return Result
     */
    List<SysRole> searchList(@Param("sysRoleDTO") SysRoleDTO sysRoleDTO,@Param("roleIdsByUser") List<Long> roleIdsByUser);
}
