package com.sunyard.module.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.api.dto.SysUserAdminDTO;
import com.sunyard.module.system.po.SysUserAdmin;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhouleibin
 * @since 2021-08-16
 */
public interface SysUserAdminMapper extends BaseMapper<SysUserAdmin> {
    /**
     * 查询用户
     * 
     * @param user 用户obj
     * @param userId 用户id
     * @param deptId 部门id
     * @return Result
     */
    List<SysUserAdminDTO> search(@Param("user") SysUserAdmin user, @Param("userId") Long userId,
        @Param("deptId") Long deptId);

    /**
     * 查询用户的个人信息
     * 
     * @param userId 用户id
     * @return Result
     */
    SysUserAdminDTO selectOrg(@Param("userId") Long userId);

    /**
     * 查询用户信息 根据 name login_name userId
     * 
     * @param thinkString 条件
     * @return Result
     */
    List<SysUserAdminDTO> selectThink(@Param("thinkString") String thinkString);
}
