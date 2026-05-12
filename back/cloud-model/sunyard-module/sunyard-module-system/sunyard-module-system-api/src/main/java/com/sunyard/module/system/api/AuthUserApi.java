package com.sunyard.module.system.api;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.sunyard.framework.common.enums.LoginTypeEnum;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.api.dto.SysUserAdminDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.constant.ApiConstants;

/**
 * @author zhouleibin
 */
@FeignClient(name = ApiConstants.NAME)
public interface AuthUserApi {

    String PREFIX = ApiConstants.PREFIX + "/authUser/";

    /**
     * 查询用户集合
     * @param userName 用户姓名
     * @return Result 用户信息list
     */
    @PostMapping(PREFIX + "search")
    Result<List<SysUserDTO>> search(@RequestParam("loginName") String userName);

    /**
     * 管理员信息查询
     * @param userName 用户姓名
     * @return Result 管理员信息list
     */
    @PostMapping(PREFIX + "adminInfoSearch")
    Result<List<SysUserAdminDTO>> adminInfoSearch(@RequestParam("loginName") String userName);

    /**
     * ldap登录查询用户信息
     * @param userName 账号
     * @param password 密码
     * @return Result 用户信息
     */
    @PostMapping(PREFIX + "ldapLogin")
    Result<List<SysUserDTO>> ldapLogin(@RequestParam("loginName") String userName,
        @RequestParam("password") String password);

    /**
     * 通过用户姓名修改用户状态
     * @param userName 用户姓名
     * @param status 状态
     * @param loginTypeEnum 登录方式。 管理员、普通用户
     * @return Result 是否修改成功
     */
    @PostMapping(PREFIX + "changeStatusByUserName")
    Result<Boolean> changeStatusByUserName(@RequestParam("userName") String userName,
        @RequestParam("status") Integer status, @RequestBody LoginTypeEnum loginTypeEnum);

    /**
     * 如果用户菜单信息
     * @param userId 用户id
     * @return Result 菜单信息
     */
    @PostMapping(PREFIX + "getRoleByUserId")
    Result<List<SysMenuDTO>> getRoleByUserId(@RequestParam(value = "userId", required = false)  Long userId);

    /**
     * 通过用户姓名解锁用户
     *
     * @param userName 用户姓名
     * @param loginTypeEnum 登录方式。 管理员、普通用户
     * @return Result 是否解锁
     */
    @PostMapping(PREFIX + "unlockByUserName")
    Result<Boolean> unlockByUserName(@RequestParam("userName") String userName, @RequestBody LoginTypeEnum loginTypeEnum);

    /**
     * 根据登录名获取邮箱
     * @param username 登录名
     * @return Result 邮箱
     */
    @PostMapping("getUserMailByUserName")
    Result<String> getUserMailByUserName(@RequestParam("username") String username);

    /**
     * 发送邮箱验证码
     * @param username 邮箱
     * @return Result 是否发送成功
     */
    @PostMapping("sendPwdMailCode")
    Result<Boolean> sendPwdMailCode(@RequestParam("userName") String username);

    /**
     * 校验验证码
     * @param username 登录名
     * @param code code
     * @return Result
     */
    @PostMapping("checkMailCodeByUserName")
    Result checkMailCodeByUserName(@RequestParam("username") String username, @RequestParam("code") String code);

    /**
     * 修改密码
     * @param username 登录名
     * @param newPwd 新密码
     * @return
     */
    @PostMapping("updatePwdByUserName")
    Result updatePwdByUserName(@RequestParam("userName") String username,
                               @RequestParam("newPwd") String newPwd,
                               @RequestParam("code") String code);

    @PostMapping(PREFIX +"updatePwdByUserId")
    Result updatePwdByUserId(@RequestParam("userId") String userId, @RequestParam("newPwd") String newPwd);

}
