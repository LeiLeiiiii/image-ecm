package com.sunyard.module.system.api;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.constant.ApiConstants;

/**
 * @description: 调用授权微服务，验证连接是否授权
 * @author: raochangmei
 * @time: 2022-10-12
 */
@FeignClient(value = ApiConstants.NAME)
public interface UserApi {

    String PREFIX = ApiConstants.PREFIX + "/user/";

    /**
     * 根据instId获取用户
     *
     * @param instId 机构id
     * @return Result
     */
    @PostMapping(PREFIX + "getUsersByInstId")
    Result<List<SysUserDTO>> getUsersByInstId(@RequestParam(value = "instId", required = false) Long instId);

    /**
     * 根据instId获取用户
     *
     * @param instId 机构id
     * @return Result
     */
    @PostMapping(PREFIX + "getUsersByInstIdList")
    Result<List<SysUserDTO>> getUsersByInstIdList(@RequestParam(value = "instId", required = false) List<Long> instId);

    /**
     * 根据instId 和name获取用户
     *
     * @param instId 机构id
     * @param name   姓名
     * @return Result
     */
    @PostMapping(PREFIX + "getUsersByInstIdAndName")
    Result<List<SysUserDTO>> getUsersByInstIdAndName(@RequestParam(value = "instId", required = false) Long instId,
                                                     @RequestParam(value = "name", required = false) String name);

    /**
     * 获取详情
     *
     * @param username 登录名
     * @return Result
     */
    @PostMapping(PREFIX + "getUserDetail")
    Result<SysUserDTO> getUserDetail(@RequestParam(value = "userName", required = false) String username);

    /**
     * 获取详情通过id
     *
     * @param userId 登录id
     * @return Result
     */
    @PostMapping(PREFIX + "getUserDetailById")
    Result<SysUserDTO> getUserDetailById(@RequestParam(value = "userId", required = false) Long userId);

    /**
     * 根据登录名集合获取用户列表
     *
     * @param loginNameList 登录名集合
     * @return Result
     */
    @PostMapping(PREFIX + "getUserByLoginName")
    Result<List<SysUserDTO>> getUserByLoginName(@RequestParam(value = "loginNameList", required = false) List<String> loginNameList);

    /**
     * 根据用户名获取用户
     *
     * @param name 姓名
     * @return Result
     */
    @PostMapping(PREFIX + "getUserDetailByName")
    Result<List<SysUserDTO>> getUserDetailByName(@RequestParam(value = "name", required = false) String name);

    /**
     * 获取详情
     *
     * @param userId 用户id
     * @return Result
     */
    @PostMapping(PREFIX + "getUserByUserId")
    Result<SysUserDTO> getUserByUserId(@RequestParam(value = "userId", required = false) Long userId);

    /**
     * 根据部门得到用户（分页）
     *
     * @param code     code
     * @param name     姓名
     * @param deptId   部门id
     * @param pageForm 分页参数
     * @return Result
     */
    @PostMapping(PREFIX + "getUserByDeptId")
    Result<Object> getUserByDeptId(@RequestParam(value = "code", required = false) String code,
                                   @RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "deptId", required = false) Long deptId,
                                   @RequestBody PageForm pageForm);

    /**
     * 根据条件得到用户（分页）
     *
     * @param code     code
     * @param name     姓名
     * @param deptId   部门id
     * @param postName 岗位名
     * @param pageForm 分页参数
     * @return Result
     */
    @PostMapping(PREFIX + "searchUserByConditions")
    Result<Object> searchUserByConditions(@RequestParam(value = "code", required = false) String code,
                                          @RequestParam(value = "name", required = false) String name,
                                          @RequestParam(value = "deptId", required = false) Long deptId,
                                          @RequestParam(value = "postName", required = false) String postName,
                                          @RequestBody PageForm pageForm);

    /**
     * 根据用户id列表获取用户信息列表
     *
     * @param userIds 用户id
     * @return Result
     */
    @PostMapping(PREFIX + "getUserListByUserIds")
    Result<List<SysUserDTO>> getUserListByUserIds(@RequestParam(value = "userIds", required = false) Long[] userIds);

    /**
     * 根据用户名列表获取用户信息列表
     *
     * @param userIds 用户id
     * @return Result
     */
    @PostMapping(PREFIX + "getUserListByUsernames")
    Result<List<SysUserDTO>> getUserListByUsernames(@RequestParam(value = "userIds", required = false) String[] userIds);

    /**
     * 根据部门、机构得到用户
     *
     * @param code   工号
     * @param name   姓名
     * @param deptId 部门id
     * @param instId 机构id
     * @return Result
     */
    @PostMapping(PREFIX + "getTransUserByDeptId")
    Result<List<SysUserDTO>> getTransUserByDeptId(@RequestParam(value = "code", required = false) String code,
                                                  @RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(value = "deptId", required = false) Long deptId,
                                                  @RequestParam(value = "instId", required = false) Long instId);

    /**
     * 根据角色id、部门id获取用户
     *
     * @param roleId 角色id
     * @param deptId 部门id
     * @return Result
     */
    @PostMapping(PREFIX + "getUserByDeptIdAndRoleId")
    Result<List<SysUserDTO>> getUserByDeptIdAndRoleId(@RequestParam(value = "deptId", required = false) Long deptId,
                                                      @RequestParam(value = "roleId", required = false) Long roleId);

    /**
     * 获取机构、部门、用户名用户列表（返回特定的机构/组织拼接的组织名称）
     *
     * @param sysRoleDTO 角色obj
     * @return Result
     */
    @PostMapping(PREFIX + "getUserList")
    Result<List<SysUserDTO>> getUserList(@RequestBody SysRoleDTO sysRoleDTO);

    /**
     * 查询用户id列表，根据用户名模糊搜索
     *
     * @param userName 登录名
     * @return Result
     */
    @PostMapping(PREFIX + "searchUserListByName")
    Result<List<Long>> searchUserListByName(@RequestParam("userName") String userName);

    /**
     * 获取用户关联的角色id列表
     *
     * @param userId 角色id
     * @return Result
     */
    @PostMapping(PREFIX + "getRoleListByUser")
    Result<List<Long>> getRoleListByUser(@RequestParam("userId") Long userId);

    /**
     * 获取用户关联的角色id列表
     *
     * @param username 登录名
     * @return Result
     */
    @PostMapping(PREFIX + "getRoleListByUsername")
    Result<List<Long>> getRoleListByUsername(@RequestParam("username") String username);

    /**
     * 查询用户信息是否存在，不存在则注册用户信息
     *
     * @param sysUserDTO 用户obj
     * @return Result
     */
    @PostMapping(PREFIX + "registerUserInfo")
    Result<SysUserDTO> registerUserInfo(@RequestBody SysUserDTO sysUserDTO);

    /**
     * 查询所有用户的机构信息
     *
     * @return Result
     */
    @PostMapping(PREFIX + "getAllUserInfo")
    Result<List<SysUserDTO>> getAllUserInfo();

    /*********************同步*************************/

    /**
     * 删除用户
     *
     * @param userIds
     * @return
     */
    @PostMapping(PREFIX + "deleteUserByUserIds")
    Result<Integer> deleteUserByUserIds(@RequestBody List<Long> userIds);

    /**
     * 编辑用户
     *
     * @param sysUserDTO
     * @return
     */
    @PostMapping(PREFIX + "updateSysUserDTO")
    Result<Integer> updateSysUserDTO(@RequestBody SysUserDTO sysUserDTO);

    /**
     * 添加用户
     *
     * @param sysUserDTO
     * @return
     */
    @PostMapping(PREFIX + "addSysUserDTO")
    Result<Integer> addSysUserDTO(@RequestBody SysUserDTO sysUserDTO);

    /**
     * 根据用户code获取用户信息包括机构code
     *
     * @return
     */
    @PostMapping(PREFIX + "queryInstCodeByUsername")
    List<SysUserDTO> queryInstCodeByUsername(@RequestBody List<String> userIds);

    /**
     * 根据传来的
     *
     * @return
     */
    @PostMapping(PREFIX + "checkUser")
    Result<SysUserDTO> checkUser(@RequestBody SysUserDTO sysUserDTO);

    /**
     * 校验用户特殊处理,角色关联特殊配置机构，不取传来的机构
     *
     * @return
     */
    @PostMapping(PREFIX + "checkUserSpecial")
    Result<SysUserDTO> checkUserSpecial(@RequestBody SysUserDTO sysUserDTO);
}
