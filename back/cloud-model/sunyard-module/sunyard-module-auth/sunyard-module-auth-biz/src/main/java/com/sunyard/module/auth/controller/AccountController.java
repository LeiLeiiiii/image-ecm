package com.sunyard.module.auth.controller;
/*
 * Project: com.sunyard.am.controller
 *
 * File Created at 2021/7/2
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.module.auth.enums.PwdSuggestStatusEnum;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.constant.LoginEncryptionConstant;
import com.sunyard.framework.common.enums.LoginTypeEnum;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.encryption.RsaUtils;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import com.sunyard.framework.common.util.ip.IpUtils;
import com.sunyard.framework.log.annotation.LoginLog;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.auth.config.properties.AuthProperties;
import com.sunyard.module.auth.config.properties.AuthVerifyCodeProperties;
import com.sunyard.module.auth.constant.CachePrefixConstants;
import com.sunyard.module.auth.shiro.realm.LdapRealm;
import com.sunyard.module.auth.shiro.realm.LoaclRealm;
import com.sunyard.module.auth.shiro.realm.SuperRealm;
import com.sunyard.module.auth.util.VerifyCodeUtils;
import com.sunyard.module.system.api.AuthOrgApi;
import com.sunyard.module.system.api.AuthParamApi;
import com.sunyard.module.system.api.AuthUserApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysConfigLoginDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

import cn.hutool.captcha.AbstractCaptcha;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @Type
 * @Desc 登录接口
 * @date 2021/7/2 14:31
 */
@Slf4j
@RestController
@RequestMapping("account")
public class AccountController extends BaseController {
    @Resource
    private AuthProperties authProperties;
    @Resource
    private AuthVerifyCodeProperties authVerifyCodeProperties;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private AuthOrgApi authOrgApi;
    @Resource
    private AuthParamApi authParamApi;
    @Resource
    private UserApi userApi;
    @Resource
    private AuthUserApi authUserApi;
    //todo 需要配置  sso开关是否开启,0未开启,1 开启
    @Value("${enableSso:0}")
    private String enableSso;

    /**
     * 登录-账号密码
     * 
     * @param username 登录名
     * @param password 密码
     * @param code 验证码
     * @param loginType 登录类型
     * @param codeId 验证码id
     * @return result
     * @throws Exception 异常
     */
    @LoginLog
    @PostMapping("login")
    public Result login(String username, String password, String code, String loginType,
                        String codeId)
            throws Exception {
        Subject subject = SecurityUtils.getSubject();
        Assert.notNull(username, "请输入登录名");
        Assert.notNull(password, "请输入密码");
        Assert.notNull(code, "请输入验证码");
        Assert.notNull(codeId, "参数错误");
        Integer loginEncryption = 1;
        switch (loginEncryption) {
            case LoginEncryptionConstant.RSA_LOGIN_TYPE:
                username = RsaUtils.decrypt(username);
                password = RsaUtils.decrypt(password);
                break;
            default:
                username = Sm2Util.decrypt(LoginEncryptionConstant.ENCRYPTED_PREFIX + username);
                password = Sm2Util.decrypt(LoginEncryptionConstant.ENCRYPTED_PREFIX + password);
        }
        String ip = IpUtils.getClientIpAddr(request);
        // 从数据库获取登录配置相关信息
        if (!StringUtils.hasText(loginType)) {
            SysConfigLoginDTO config = authParamApi.select().getData();
            if (config == null || "0".equals(config.getLoginType().toString())) {
                loginType = LoginTypeEnum.LOCAL.getValue();
            } else if ("1".equals(config.getLoginType().toString())) {
                loginType = LoginTypeEnum.LDAP.getValue();
            }
        }
        // 校验验证码
        if (authVerifyCodeProperties.getEnable()) {
            this.checkPicCode(code, codeId);
        }

        LoginTypeEnum loginTypeEnum = null == LoginTypeEnum.getType(loginType) ? LoginTypeEnum.LOCAL
                : LoginTypeEnum.getType(loginType);

        // 校验密码错误次数，并判断是否属于锁定状态
        this.checkLoginErrorNum(username, ip, loginTypeEnum);

        AccountToken loginToken = new AccountToken();
        loginToken.setUsername(username);
        loginToken.setPassword(password.toCharArray());
        try {
            // 登录方式设置
            switch (Objects.requireNonNull(loginTypeEnum)) {
                case LDAP:
                    setUserDetailToToken(username, loginToken);
                    loginToken.setRealmType(LdapRealm.class.getSimpleName());
                    break;
                case SUPER:
                    loginToken.setRealmType(SuperRealm.class.getSimpleName());
                    break;
                default:
                    setUserDetailToToken(username, loginToken);
                    loginToken.setRealmType(LoaclRealm.class.getSimpleName());
                    break;
            }
            subject.login(loginToken);
            redisUtils.del(CachePrefixConstants.LOGIN_ACCOUNT_ERROR_LIMIT + username);
            redisUtils.del(CachePrefixConstants.LOGIN_IP_ERROR_LIMIT + ip);
            return Result.success(this.getLoginResult(loginToken, loginTypeEnum));
        } catch (UnknownAccountException | IncorrectCredentialsException e) {
            redisUtils.incr(CachePrefixConstants.LOGIN_ACCOUNT_ERROR_LIMIT + username,
                    TimeOutConstants.ONE_HOURS);
            redisUtils.incr(CachePrefixConstants.LOGIN_IP_ERROR_LIMIT + ip,
                    TimeOutConstants.ONE_DAY);
            subject.logout();
            log.error(e.getMessage());
            return Result.error("登录名或密码错误，请重新输入!", ResultCode.PARAM_ERROR);
        } catch (ShiroException e) {
            log.error(e.getMessage());
            return Result.error(e.getMessage(), ResultCode.PARAM_ERROR);
        } catch (SunyardException e) {
            return Result.error("登录名或密码错误，请重新输入!", ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 登出
     * 
     * @param username 登录名 (aop日志记录用，方法里没用到)
     * @return result
     */
    @LoginLog
    @PostMapping("logout")
    public Result logout(String username) {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            subject.logout();
        }
        return Result.success(true);
    }

    /**
     * 获得验证码-图片
     * 
     * @return Result
     */
    @PostMapping("QRCode")
    public Result getVerifyCode() {
        String verifyCodeKey = CachePrefixConstants.PIC_QR_CODE
                + SecurityUtils.getSubject().getSession().getId().toString();
        AbstractCaptcha captcha = VerifyCodeUtils
                .generateVerifyCode(authVerifyCodeProperties.getType());
        String verifyCode = captcha.getCode().toUpperCase();
        log.debug(" *** 图片验证码 {} *** ", verifyCode);
        redisUtils.set(verifyCodeKey, verifyCode, 30L, TimeUnit.MINUTES);
        Map<String, Object> map = new HashMap<>(10);
        map.put("CodeId", SecurityUtils.getSubject().getSession().getId().toString());
        map.put("VCode", captcha.getImageBase64());
        return Result.success(map);
    }

    /**
     * 发送邮箱验证码
     *
     * @param username 登录名
     * @return Result
     */
    @PostMapping("sendMailCode")
    public Result sendMailCode(String username) {
        return authUserApi.sendPwdMailCode(username);
    }

    /**
     * 校验验证码
     *
     * @param username 登录名
     * @param code 验证码
     * @return Result
     */
    @PostMapping("checkMailCode")
    public Result checkMailCode(String username, String code) {
        return authUserApi.checkMailCodeByUserName(username, code);
    }

    /**
     * 更新密码
     *
     * @param username 登录名
     * @param newPwd 新密码
     * @return Result
     */
    @PostMapping("updatePwd")
    public Result updatePwd(String username, String newPwd, String code) throws Exception {
        newPwd = RsaUtils.decrypt(newPwd);
        return authUserApi.updatePwdByUserName(username, newPwd, code);
    }

    /**
     * 校验账号登录错误是否超出范围
     *
     * @param userName 用户名
     * @param ip ip地址
     * @param loginTypeEnum 登录类型
     */
    private void checkLoginErrorNum(String userName, String ip, LoginTypeEnum loginTypeEnum) {
        // 判断同一用户错误登录次数是否超限 5次
        String loginLimit = redisUtils
                .get(CachePrefixConstants.LOGIN_ACCOUNT_ERROR_LIMIT + userName);
        if (StringUtils.hasText(loginLimit) && Integer.parseInt(loginLimit) > 5) {
            // 锁定此用户
            authUserApi.changeStatusByUserName(userName, 3, loginTypeEnum);
            throw new SunyardException(ResultCode.PARAM_ERROR, "当前用户连续认证失败次数超限，请一小时后再试");
        }
        // 判断同一ip错误登录次数是否超限 50次
        String ipLoginLimit = redisUtils.get(CachePrefixConstants.LOGIN_IP_ERROR_LIMIT + ip);
        if (StringUtils.hasText(ipLoginLimit) && Integer.parseInt(ipLoginLimit) > 50) {
            throw new SunyardException(ResultCode.PARAM_ERROR, "当前ip连续认证失败次数超限，请一小时后再试");
        }
    }

    /**
     * 校验账号登录图片验证码正确性
     *
     * @param verifyCode 验证码
     * @param codeId 验证码id
     */
    private void checkPicCode(String verifyCode, String codeId) {
        verifyCode = verifyCode.toUpperCase();
        String verifycodeKey = CachePrefixConstants.PIC_QR_CODE + codeId;
        String cacheCode = redisUtils.get(verifycodeKey);
        // 删除验证码，请求之后验证码失效
        redisUtils.del(verifycodeKey);
        if (!StringUtils.hasText(cacheCode)) {
            log.debug("您的验证码已过期");
            throw new SunyardException(ResultCode.PARAM_ERROR, "您的验证码已过期");
        }
        if (!verifyCode.equals(cacheCode.substring(0, 5))) {
            log.debug("验证码有误，请重新输入");
            throw new SunyardException(ResultCode.PARAM_ERROR, "验证码有误，请重新输入");
        }
    }

    /**
     * 构造登录后返回数据 0 代表密码正常不用修改 1代表30天内未修改密码，建议修改 2代表初始密码，需要去修改
     *
     * @return Result
     */
    private Map getLoginResult(AccountToken accountToken, LoginTypeEnum loginTypeEnum) {
        int pwdSuggest = 0;
        //sso开关是否开启
        if (enableSso.equals("1")){
            if (authProperties.getPasswordCheck()) {
                char[] storedPassword = authProperties.getUserInitPassword().toCharArray();
                char[] inputPassword = accountToken.getPassword();

                try {
                    if (Arrays.equals(storedPassword, inputPassword)) {
                        pwdSuggest = 2;
                    }
                } finally {
                    // 清除敏感数据
                    Arrays.fill(storedPassword, ' ');
                    Arrays.fill(inputPassword, ' ');
                }
            }
            if (authProperties.getPasswordCheck() && pwdSuggest == 0) {
                Calendar oldTime = Calendar.getInstance();
                oldTime.setTime(accountToken.getPwdUpdateTime());
                oldTime.add(Calendar.MONTH, 1);
                if (oldTime.getTime().before(new Date())) {
                    pwdSuggest = 1;
                }
            }
        }
        Map<String, Object> map = new HashMap<>(10);
        map.put("pwdSuggest", pwdSuggest + "");
        if (LoginTypeEnum.SUPER.equals(loginTypeEnum)) {
            map.put("user", authOrgApi.selectSuperOrg(getToken().getId()).getData());
        } else {
            map.put("user", authOrgApi.selectOrg(getToken().getId()).getData());
        }
        map.put("AuthorizationToken", SecurityUtils.getSubject().getSession().getId().toString());
        // 添加用户类型
        map.put("loginType", "1".equals(enableSso)?"998":accountToken.getLoginType());
        return map;
    }

    /**
     * token赋组织角色属性
     */
    private void setUserDetailToToken(String username, AccountToken loginToken) {
        try {
            Result<SysUserDTO> userDetail = userApi.getUserDetail(username);
            if (userDetail.isSucc()) {
                loginToken.setOrgCode(userDetail.getData().getInstNo());
                loginToken.setDeptCode(userDetail.getData().getDeptCode());
                loginToken.setRoleIdList(userDetail.getData().getRoleIdList());
                loginToken.setRoleCodeList(userDetail.getData().getRoleCodeList());
            }
        } catch (Exception e) {
            throw new SunyardException(ResultCode.PARAM_ERROR, e.getMessage());
        }
    }

}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/2 zhouleibin creat
 */
