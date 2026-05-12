package com.sunyard.module.auth.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.module.auth.config.properties.AuthSsoCasProperties;
import com.sunyard.module.auth.shiro.realm.SsoRealm;
import com.sunyard.module.system.api.AuthOrgApi;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @Desc 单点登录入口
 * @date 2025/7/2 13:33
 */
@Slf4j
@RestController
@RequestMapping("sso")
public class SsoController extends BaseController {

    @Resource
    private AuthOrgApi authOrgApi;
    @Resource
    private AuthSsoCasProperties authSsoCasProperties;

    /**
     * 登录-ticket
     * @return result
     * @throws Exception 异常
     */
    @PostMapping("login")
    public Result login(@RequestParam("username") String username) {
        Subject subject = SecurityUtils.getSubject();
        Assert.notNull(username, "票据参数错误");

        AccountToken loginToken = new AccountToken();
        loginToken.setUsername(username);
        loginToken.setRealmType(SsoRealm.class.getSimpleName());
        try {
            subject.login(loginToken);
            return Result.success(this.getLoginResult(loginToken));
        } catch (UnknownAccountException | IncorrectCredentialsException e) {
            subject.logout();
            log.error(e.getMessage());
            return Result.error(e.getMessage(), ResultCode.PARAM_ERROR);
        } catch (ShiroException e) {
            log.error(e.getMessage());
            return Result.error(e.getMessage(), ResultCode.PARAM_ERROR);
        } catch (SunyardException e) {
            return Result.error("用户不存在!", ResultCode.PARAM_ERROR);
        }
    }

    /** */
    @PostMapping("getSsoLoginUrl")
    public Result getSsoLoginUrl() {
        Assert.notNull(authSsoCasProperties.getServerUrl(), "CAS服务地址未配置");
        StringBuffer loginUrl = new StringBuffer();
        loginUrl.append(authSsoCasProperties.getServerUrl()).append("login");
        loginUrl.append("?appid=").append(authSsoCasProperties.getAppId());
        loginUrl.append("&service=").append(authSsoCasProperties.getCallbackUrl());
        return Result.success(loginUrl.toString());
    }

    //    /**
    //     * 登出接口（增强支持CAS登出）
    //     */
    //    @LoginLog
    //    @PostMapping("logout")
    //    public Result logout(String username, HttpServletResponse response) {
    //        Subject subject = SecurityUtils.getSubject();
    //        if (subject.isAuthenticated()) {
    //            // 本地登出
    //            subject.logout();
    //            // CAS全局登出
    //            String casLogoutUrl = casConfig.getCasServerLogoutUrl() + "/logout";
    //            return Result.success(casLogoutUrl);
    //        }
    //        return Result.success(true);
    //    }

    /**
     * 构造登录后返回数据 0 代表密码正常不用修改 1代表30天内未修改密码，建议修改 2代表初始密码，需要去修改
     *
     * @return Result
     */
    private Map getLoginResult(AccountToken accountToken) {
        Map<String, Object> map = new HashMap<>(10);
        map.put("user", authOrgApi.selectOrg(getToken().getId()).getData());
        map.put("AuthorizationToken", SecurityUtils.getSubject().getSession().getId().toString());
        // 添加用户类型
        map.put("loginType", accountToken.getLoginType());
        return map;
    }

}
