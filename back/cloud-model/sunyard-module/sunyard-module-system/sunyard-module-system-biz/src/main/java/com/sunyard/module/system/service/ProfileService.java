package com.sunyard.module.system.service;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.sunyard.module.system.constant.CachePrefixConstants;
import org.dromara.email.api.MailClient;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.PasswordUtils;
import com.sunyard.framework.common.util.RegexpUtils;
import com.sunyard.framework.common.util.UUIDUtils;
import com.sunyard.framework.message.util.EmailUtils;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.mapper.SysUserAdminMapper;
import com.sunyard.module.system.mapper.SysUserMapper;
import com.sunyard.module.system.po.SysUser;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.GifCaptcha;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @date 2021/9/6 16:27
 * @Desc
 */
@Slf4j
@Service
public class ProfileService {

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private EmailUtils emailUtils;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysUserAdminMapper sysUserAdminMapper;

    /**
     * 查询用户个人信息
     * @param userId 用户id
     * @return Result
     */
    public Result selectOrg(Long userId) {
        AssertUtils.isNull(userId, "参数错误");
        SysUserDTO result = sysUserMapper.selectOrg(userId);
        return Result.success(result);
    }

    /**
     * 查询超级管理员信息
     * @param id 用户id
     * @return Result
     */
    public Result selectSuperOrg(Long id) {
        Assert.notNull(id, "参数错误");
        return Result.success(sysUserAdminMapper.selectOrg(id));
    }

    /**
     * 用户修改基本资料
     * @param user 用户obj
     * @return Result
     */
    public Result update(SysUser user) {
        if (StringUtils.hasText(user.getEmail())) {
            Assert.isTrue(RegexpUtils.isEmail(user.getEmail()), "请填写正确的邮箱");
        }
        SysUser upUser = new SysUser();
        upUser.setUserId(user.getUserId());
        upUser.setName(user.getName());
        upUser.setPhone(user.getPhone());
        upUser.setEmail(user.getEmail());
        upUser.setSex(user.getSex());
        sysUserMapper.updateById(upUser);
        return Result.success(true);
    }

    /**
     * 修改密码
     * @param userId 用户id
     * @param oldPwd 老密码
     * @param newPwd 新密码
     * @param mailCode 邮箱code
     * @return Result
     */
    public Result updateUserPwd(Long userId, String oldPwd, String newPwd, String mailCode) {
        Assert.isTrue(PasswordUtils.passwordValidator(newPwd), "密码校验不合法,请检查并重新输入重试...");
        SysUser user = sysUserMapper.selectById(userId);
        Assert.notNull(user, "用户不存在");
        oldPwd = PasswordUtils.getEncryptionPassword(user.getSalt(), oldPwd);
        Assert.isTrue(user.getPwd().equals(oldPwd), "原密码错误");
        String salt = UUIDUtils.generateUUID();
        newPwd = PasswordUtils.getEncryptionPassword(salt, newPwd);
        user.setPwd(newPwd);
        user.setSalt(salt);
        user.setUpdateTime(new Date());
        user.setPwdUpdateTime(new Date());
        sysUserMapper.updateById(user);
        return Result.success(true);
    }

    /**
     * 获取当前用户类型
     * @param userId 用户id
     * @return Result
     */
    public Result selectUserType(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        Assert.notNull(user, "用户不存在");
        return Result.success(user.getType());
    }

    /**
     * 获取邮箱验证码
     * @return Result
     */
    public Result getEmailCode(String username) {
        SysUser sysUser = sysUserMapper
                .selectOne(new LambdaUpdateWrapper<SysUser>().eq(SysUser::getLoginName, username));
        Assert.notNull(sysUser.getEmail(), "请先配置邮箱");
        GifCaptcha gifCaptcha = CaptchaUtil.createGifCaptcha(130, 48);
        String verifyCode = gifCaptcha.getCode().toUpperCase();
        String emailUtilsContent = "您正在进行密码重置操作!\n" + "您的邮箱验证码为:%s，请勿向他人透露！有效期10分钟";
        try {
            MailClient mailClient = emailUtils.getMailClient();
            mailClient.sendMail(sysUser.getEmail(), "忘记密码",
                    String.format(emailUtilsContent, verifyCode));
        } catch (Exception e) {
            log.error("系统异常", e);
            return Result.error("发送验证码失败！请联系管理员", ResultCode.PARAM_ERROR);
        }
        redisUtils.set(CachePrefixConstants.FORGOT_PASS + username, verifyCode, 600L, TimeUnit.SECONDS);
        return Result.success();
    }
}
