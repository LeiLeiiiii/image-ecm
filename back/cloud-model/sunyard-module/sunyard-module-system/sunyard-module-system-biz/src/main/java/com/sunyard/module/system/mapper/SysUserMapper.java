package com.sunyard.module.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.api.dto.SysUserExportDTO;
import com.sunyard.module.system.po.SysUser;
import com.sunyard.module.system.vo.SysUserVO;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author zhouleibin
 * @since 2021-08-16
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * 查询用户
     *
     * @param user 用户obj
     * @param userId 用户id
     * @param deptId 部门id
     * @return Result
     */
    List<SysUserDTO> search(@Param("user") SysUserVO user, @Param("userId") Long userId, @Param("deptId") Long deptId);

    /**
     * 查询用户的个人信息
     *
     * @param userId 用户id
     * @return Result
     */
    SysUserDTO selectOrg(@Param("userId") Long userId);

    /**
     * 查询用户信息 根据 name login_name userId
     *
     * @param thinkString 条件
     * @return Result
     */
    List<SysUserDTO> selectThink(@Param("thinkString") String thinkString);

    /**
     * 查询用户信息 根据部门条件
     *
     * @param name 名称
     * @param deptId 机构id
     * @param code code
     * @return Result
     */
    List<SysUserDTO> selectByDeptId(
            @Param("name") String name,
            @Param("deptId") Long deptId,
            @Param("code") String code,
            @Param("instId") Long instId
    );

    /**
     * 查询用户信息 根据部门条件
     *
     * @param name 用户名
     * @param deptId 部门id
     * @param code 用户工号
     * @param postName 岗位
     * @return Result
     */
    List<SysUserDTO> searchUserByConditions(
            @Param("name") String name,
            @Param("deptId") Long deptId,
            @Param("code") String code,
            @Param("postName") String postName
    );

    /**
     * 查询部门下指定角色的用户
     *
     * @param roleName 角色名称
     * @param deptId 部门id
     * @param instId 机构id
     * @return Result
     */
    List<SysUser> searchRoleByDpt(@Param("roleName") String roleName, @Param("deptId") Long deptId,
        @Param("instId") Long instId);

    /**
     * 根据角色和部门查询
     *
     * @param instId 机构id
     * @param deptId 部门id
     * @param roleId 角色id
     * @param type 类型
     * @return Result
     */
    List<SysUserDTO> getUserByRoleIdorDeptId(
            @Param("instId") Long instId,
            @Param("deptId") Long deptId,
            @Param("roleId") Long roleId,
            @Param("type") String type
    );

    /**
     * 导出
     * @return Result
     */
    List<SysUserExportDTO> exportList();

    /**
     * 查询所有用户的机构信息
     * @return Result
     */
    List<SysUserDTO> getAllUserInfo();
}
