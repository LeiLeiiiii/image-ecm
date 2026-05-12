package com.sunyard.module.system.controller;
/*
 * Project: am
 *
 * File Created at 2021/7/14
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.Arrays;

import javax.annotation.Resource;

import com.sunyard.framework.common.constant.LoginEncryptionConstant;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.encryption.RsaUtils;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.dto.SysPostUserDTO;
import com.sunyard.module.system.po.SysUser;
import com.sunyard.module.system.service.OrgUserService;
import com.sunyard.module.system.service.ProfileService;
import com.sunyard.module.system.vo.SysUserVO;

/**
 * @author zhouleibin
 * @Type
 * @Desc 基础管理/通用管理/用户管理
 * @date 2021/7/14 17:42
 */
@RestController
@RequestMapping("basics/currency/user")
public class BasicsCurrencyUserController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_CURRENCY + "-用户管理->";
    @Resource
    private OrgUserService orgUserService;
    @Resource
    private ProfileService profileService;

    /**
     * 用户详情
     *
     * @param userId 用户id
     * @return result
     */
    @OperationLog(BASELOG + "用户详情")
    @PostMapping("select")
    public Result select(Long userId) {
        return Result.success(orgUserService.select(userId));
    }

    /**
     * 获取用户列表
     *
     * @param user     用户obj
     * @param pageForm 分页参数
     * @return result
     */
    @OperationLog(BASELOG + "获取用户列表")
    @PostMapping("search")
    public Result search(SysUserVO user, PageForm pageForm) {
        if (null == user.getInstId() && null == user.getDeptId()) {
            user.setInstId(getToken().getInstId());
        } else {
            user.setInstId(null == user.getInstId() ? getToken().getInstId() : user.getInstId());
            user.setDeptId(null == user.getDeptId() ? Long.valueOf(0L) : user.getDeptId());
        }
        return Result.success(orgUserService.search(user, pageForm, getToken().getId(), getToken().getDeptId()));
    }

    /**
     * 新增用户
     *
     * @param user 用户obj
     * @return result
     */
    @OperationLog(BASELOG + "新增用户")
    @PostMapping("add")
    public Result add(SysUserVO user) {
        orgUserService.add(user);
        return Result.success(true);
    }

    /**
     * 编辑用户
     *
     * @param user 用户obj
     * @return result
     */
    @OperationLog(BASELOG + "编辑用户")
    @PostMapping("update")
    public Result update(SysUserVO user) {
        if (user.getUserId().equals(getToken().getId())){
            Assert.notNull(null, "禁止编辑自身权限");
        }
        orgUserService.update(user);
        return Result.success(true);
    }

    /**
     * 删除用户
     *
     * @param userIds 用户id集
     * @return result
     */
    @OperationLog(BASELOG + "删除用户")
    @PostMapping("del")
    public Result del(Long[] userIds) {
        Assert.notEmpty(userIds, "参数错误");
        if (Arrays.asList(userIds).contains(getToken().getId())) {
            Assert.notNull(null, "禁止删除自身用户");
        }
        orgUserService.del(userIds);
        return Result.success(true);
    }

    /**
     * 查询角色
     *
     * @param instId 机构id
     * @param deptId 部门id
     * @return result
     */
    @OperationLog(BASELOG + "查询角色")
    @PostMapping("searchRole")
    public Result searchRole(Long instId, Long deptId) {
        return Result.success(orgUserService.searchRole(instId, deptId));
    }

    /**
     * 编辑用户角色
     *
     * @param roleIds 角色id集
     * @param ids     用户id集
     * @return result
     */
    @OperationLog(BASELOG + "编辑用户角色")
    @PostMapping("updateRole")
    public Result updateRole(Long[] roleIds, Long[] ids) {
        orgUserService.updateRole(roleIds, ids);
        return Result.success(true);
    }

    /**
     * 编辑用户岗位
     *
     * @param sysPostUserDTO 岗位用户id集
     * @return result
     */
    @OperationLog(BASELOG + "编辑用户岗位")
    @PostMapping("updatePost")
    public Result updatePost(@RequestBody SysPostUserDTO sysPostUserDTO) {
        orgUserService.updatePost(sysPostUserDTO);
        return Result.success(true);
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户id
     * @param status 状态 未启用:0,启用:1,注销:2,锁定:3
     * @return result
     */
    @OperationLog(BASELOG + "修改用户状态")
    @PostMapping("changeUserStatus")
    public Result changeUserStatus(Long userId, Integer status) {
        orgUserService.changeUserStatus(userId, status);
        return Result.success(true);
    }

    /**
     * 重置密码
     *
     * @param userIds 用户id集
     * @return result
     */
    @OperationLog(BASELOG + "重置密码")
    @PostMapping("resetPwd")
    public Result resetPwd(Long[] userIds) {
        orgUserService.resetPwd(userIds);
        return Result.success(true);
    }

    /**
     * 查询指定部门用户
     *
     * @param code     用户工号
     * @param name     用户姓名
     * @param deptId   部门id
     * @param pageForm 分页参数
     * @return result
     */
    @OperationLog(BASELOG + "查询指定部门用户")
    @PostMapping("getUserByDeptId")
    public Result getUserByDeptId(String code, String name, Long deptId, PageForm pageForm) {
        deptId = deptId == null ? getToken().getDeptId() : deptId;
        return Result.success(orgUserService.getUserByDeptId(code, name, deptId, pageForm));
    }

    /**
     * 更新扫描权限
     *
     * @param userId 用户id
     * @param isScan 是否有扫描权限 0是 1否
     * @return result
     */
    @OperationLog(BASELOG + "更新扫描权限")
    @PostMapping("updateIsScan")
    public Result updateIsScan(Long userId, Integer isScan) {
        orgUserService.updateIsScan(userId, isScan);
        return Result.success(true);
    }

    /**
     * 根据角色id查询用户列表
     *
     * @param roleId 角色id
     * @return result
     */
    @OperationLog(BASELOG + "根据角色id查询用户列表")
    @PostMapping("getUserByRoleId")
    public Result getUserByRoleId(Long roleId) {
        return Result.success(orgUserService.getUserByRoleId(roleId));
    }

    /**
     * 根据角色id或者部门id查询用户列表
     *
     * @param roleId   角色id
     * @param deptId   部门id
     * @param pageForm 分页参数
     * @return result
     */
    @OperationLog(BASELOG + "根据角色id或者部门id查询用户列表")
    @PostMapping("getUserByRoleIdOrDeptId")
    public Result getUserByRoleIdOrDeptId(Long roleId, Long deptId, PageForm pageForm) {
        return Result.success(orgUserService.getUserByRoleIdOrDeptId(roleId, deptId, pageForm, getToken().getInstId()));
    }

    /**
     * 用户修改密码
     *
     * @param oldPwd   老密码
     * @param newPwd   新密码
     * @param mailCode 邮箱验证码
     * @return result
     * @throws Exception 异常
     */
    @OperationLog(BASELOG + "用户修改密码")
    @PostMapping("updatePwd")
    public Result updatePwd(String oldPwd, String newPwd, String mailCode) throws Exception {
        Integer loginEncryption = 1;
        switch (loginEncryption) {
            case LoginEncryptionConstant.RSA_LOGIN_TYPE:
                oldPwd = RsaUtils.decrypt(oldPwd);
                newPwd = RsaUtils.decrypt(newPwd);
                break;
            default:
                oldPwd = Sm2Util.decrypt(LoginEncryptionConstant.ENCRYPTED_PREFIX + oldPwd);
                newPwd = Sm2Util.decrypt(LoginEncryptionConstant.ENCRYPTED_PREFIX + newPwd);
        }
        return profileService.updateUserPwd(getToken().getId(), oldPwd, newPwd, mailCode);
    }

    /**
     * 用户修改基本资料
     *
     * @param user 用户obj
     * @return result
     */
    @OperationLog(BASELOG + "用户修改基本资料")
    @PostMapping("updatePersonalInfo")
    public Result updatePersonalInfo(@RequestBody SysUser user) {
        user.setUserId(getToken().getId());
        return profileService.update(user);
    }

    /**
     * 获取当前用户类型
     *
     * @return result
     */
    @OperationLog(BASELOG + "获取当前用户类型")
    @PostMapping("selectType")
    public Result selectUserType() {
        return profileService.selectUserType(getToken().getId());
    }

    /**
     * 获取邮箱验证码(供修改密码时使用)
     *
     * @return result
     */
    @OperationLog(BASELOG + "获取邮箱验证码(供修改密码时使用)")
    @PostMapping("getEmailCode")
    public Result getEmailCode() {
        return profileService.getEmailCode(getToken().getUsername());
    }
}
