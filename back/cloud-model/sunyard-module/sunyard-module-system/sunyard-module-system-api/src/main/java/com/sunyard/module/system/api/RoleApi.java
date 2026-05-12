package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import com.sunyard.module.system.api.dto.SysRoleUserDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @description: 调用授权微服务，验证连接是否授权
 * @author: raochangmei
 * @time: 2022-10-12
 */
@FeignClient(value = ApiConstants.NAME)
public interface RoleApi {

    String PREFIX = ApiConstants.PREFIX + "/role/";

    /**
     * 根据用户id获取角色
     *
     * @param userId 用户id
     * @return Result
     */
    @PostMapping(PREFIX + "getRoleByUserId")
    Result<List<SysRoleDTO>> getRoleByUserId(@RequestParam("userId") Long userId);

    /**
     * 根据用户id获取角色
     *
     * @param userIds 用户id
     * @return Result
     */
    @PostMapping(PREFIX + "getRoleUserByUserIds")
    Result<List<SysRoleDTO>> getRoleByUserIds(@RequestParam("userIds") Long[] userIds);

    /**
     * 根据id获取角色信息
     *
     * @param roleIds 角色id
     * @return Result
     */
    @PostMapping(PREFIX + "getRoleById")
    Result<List<SysRoleDTO>> getRoleById(@RequestParam("roleIds") Long[] roleIds);

    /**
     * 根据机构id获取角色信息
     *
     * @param instId 机构id
     * @return Result
     */
    @PostMapping(PREFIX + "getRoleByInstId")
    Result<List<SysRoleDTO>> getRoleByInstId(@RequestParam(value = "instId", required = false) Long instId);

    /**
     * 新增角色
     * @param sysRoleDTO 角色 dto
     * @return Result
     */
    @PostMapping(PREFIX +  "add")
    Result<Long> add(@RequestBody SysRoleDTO sysRoleDTO);

    /**
     * 删除角色
     * @param roleId 角色id
     * @return Result
     */
    @PostMapping(PREFIX + "delete")
    Result delete(@RequestParam("roleId") Long roleId);

    /**
     * 批量删除角色
     * @param roleIds 角色id
     * @return Result
     */
    @PostMapping(PREFIX + "lotDelete")
    Result lotDelete(@RequestBody List<Long> roleIds);

    /**
     * 获取角色列表
     * @param sysRoleDTO 角色obj
     * @return Result
     */
    @PostMapping(PREFIX + "searchList")
    Result searchList(@RequestBody SysRoleDTO sysRoleDTO);


    /**
     * 获取角色列表,查配置了影像权限的
     * @param sysRoleDTO 角色obj
     * @return Result
     */
    @PostMapping(PREFIX + "searchListInUsePage")
    Result searchListInUsePage(@RequestBody SysRoleDTO sysRoleDTO);

    /**
     * 角色详情
     * @param roleId 角色id
     * @return Result
     */
    @PostMapping(PREFIX + "roleDetails")
    Result<SysRoleDTO> roleDetails(@RequestParam("roleId") Long roleId);

    /**
     * 角色编辑
     * @param sysRoleDTO 角色obj
     * @return Result
     */
    @PostMapping(PREFIX + "roleEdit")
    Result roleEdit(@RequestBody SysRoleDTO sysRoleDTO);

    /**
     * 关联用户
     * @param sysRoleDTO 角色obj
     * @return Result
     */
    @PostMapping(PREFIX + "relateUser")
    Result relateUser(@RequestBody SysRoleDTO sysRoleDTO);

    /**
     * 获取当前角色关联用户列表
     * @param roleId 角色id
     * @return Result
     */
    @PostMapping(PREFIX + "getRelateUserList")
    Result<List<SysUserDTO>> getRelateUserList(@RequestParam("roleId") Long roleId);

    /**
     * 根据角色id获取菜单
     *
     * @param roleIdList 角色id
     * @return Result
     */
    @PostMapping(PREFIX + "getMenuByRoleId")
    Result getMenuByRoleId(@RequestBody List<Long> roleIdList);

    /**
     * 根据模块id获取角色
     *
     * @param menuId 菜单id
     * @return Result
     */
    @PostMapping(PREFIX + "getRoleListByMenuId")
    Result<List<SysRoleDTO>> getRoleListByMenuId(@RequestParam("menuId") Long menuId);

    /**
     * 根据角色code获取角色信息
     *
     * @param roleCode 角色code
     * @param systemCode 系统Code 0档案 1影像
     * @return Result
     */
    @PostMapping(PREFIX + "getRoleByRoleCode")
    Result<SysRoleDTO> getRoleByRoleCode(@RequestParam("roleCode") String roleCode,@RequestParam("systemCode") Integer systemCode);


    /*********************同步*************************/

    /**
     * 删除角色用户的关联关系
     * @param userIds
     * @return
     */
    @PostMapping(PREFIX + "deleteRoleUserByUserIds")
    Result<Integer> deleteRoleUserByUserIds(@RequestBody List<Long> userIds);

    /**
     * 关联用户角色 保留之前的关联关系
     * @param sysRoleUserDTO
     * @return
     */
    @PostMapping(PREFIX + "relateUserRole")
    Result<Integer> relateUserRole(@RequestBody SysRoleUserDTO sysRoleUserDTO);
}
