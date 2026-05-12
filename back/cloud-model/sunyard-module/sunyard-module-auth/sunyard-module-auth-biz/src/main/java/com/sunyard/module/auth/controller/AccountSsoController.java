package com.sunyard.module.auth.controller;

import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.result.SsoResultCode;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.auth.service.SsoService;
import com.sunyard.module.auth.config.properties.AuthProperties;
import com.sunyard.module.auth.controller.dto.UpdatePasswordRequest;
import com.sunyard.module.system.api.AuthOrgApi;
import com.sunyard.module.system.api.AuthUserApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.session.Session;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.util.*;

/**
 * 三井 sso对接接口
 */
@Slf4j
@RestController
@RequestMapping("account/sso")
public class AccountSsoController extends BaseController {

    @Resource
    private SsoService ssoService;
    @Resource
    private UserApi userApi;
    @Resource
    private AuthOrgApi authOrgApi;
    @Resource
    private AuthUserApi authUserApi;
    @Resource
    private AuthProperties authProperties;

    /**
     * 触发SSO登录，重定向到客户认证平台获取code
     */
    @PostMapping("/login")
    public Result ssoLogin(HttpServletResponse response) throws IOException {
        String redirectUrl = ssoService.getCallBackUrl();
        // 执行重定向
//        response.sendRedirect(redirectUrl);
        return Result.success(redirectUrl);
    }

    /**
     * 客户回调地址，拿到code -> 获取token - > 获取用户信息
     */
    @PostMapping("/callback")
    public Result ssoCallback(@RequestParam("code") String code, HttpServletRequest request) {

        try {
            //获取登录用户信息
            String userName = ssoService.handleSsoCallback(code, request);
//            String userName = "xl";
            log.info("SSO callback for user: {}", userName);

            // 1. 获取用户详情
            Result<SysUserDTO> userDetail = userApi.getUserDetail(userName);
            if (!userDetail.isSucc() || userDetail.getData() == null) {
                log.error("User not found: {}", userName);
                return Result.error("用户不存在!", ResultCode.PARAM_ERROR);
            }
            SysUserDTO user = userDetail.getData();
            log.info("User details obtained: {}", user.getLoginName());

            // 2. 创建 AccountToken 并设置所有信息
            AccountToken loginToken = new AccountToken();
            loginToken.setUsername(userName);
            loginToken.setId(user.getUserId());
            loginToken.setName(user.getName());
            loginToken.setDeptId(user.getDeptId());
            loginToken.setInstId(user.getInstId());
            loginToken.setOrgName(user.getInstName());
            loginToken.setLoginType(user.getType());
            loginToken.setOrgCode(user.getInstNo());
            loginToken.setDeptCode(user.getDeptCode());
            loginToken.setRoleIdList(user.getRoleIdList());
            loginToken.setRoleCodeList(user.getRoleCodeList());
            // 设置 realm 类型为 SsoRealm
            loginToken.setRealmType("SsoRealm");

            // 3. 使用 Shiro 登录进行正式认证
            Subject subject = SecurityUtils.getSubject();
            // 检查是否已有会话，实现会话复用
            if (!subject.isAuthenticated()) {
                subject.login(loginToken);
                log.info("User authenticated successfully: {}", userName);
            } else {
                log.info("User already authenticated: {}", userName);
            }
            // 获取会话并设置过期时间
            Session session = subject.getSession();
            long timeout = authProperties.getSessionIdExpire() != null 
                ? authProperties.getSessionIdExpire() * 1000 
                : 180000; // 默认3分钟
            session.setTimeout(timeout);
            log.info("Session timeout set to: {} ms, sessionId: {}", timeout, session.getId());

            // 4. 返回登录结果
            Map<String, Object> result = this.getLoginResult(loginToken);
            log.info("Login result generated with token: {}", result.get("AuthorizationToken"));
            return Result.success(result);

        } catch (Exception e) {
            log.error("SSO callback failed", e);
            return Result.error("SSO登录失败: " + e.getMessage(), ResultCode.PARAM_ERROR);
        }
    }

    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody UpdatePasswordRequest request) {
        String appId = request.getAppId();
        String appSecret = request.getAppSecret();
        String userId = request.getUserId();
        String password = request.getPassword();
        log.info("[密码同步] 接收到请求 | appId={} | appSecret={} | userId={} | password(加密)={}", appId, appSecret, userId, password);
        try {
            //参数校验
            if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret) || StringUtils.isBlank(userId) || StringUtils.isBlank(password)) {
                log.warn("[密码同步] 参数缺失 | appId={} | userId={}", appId, userId);
                return Result.ssoResponse(SsoResultCode.PARAM_MISSING);
            }
            //认证校验
            if (!ssoService.checkApp(appId, appSecret)) {
                log.warn("[密码同步] 认证失败 | appId={} | userId={}", appId, userId);
                return Result.ssoResponse(SsoResultCode.AUTH_FAILED);
            }
            //用户校验 - 通过loginName查询
            List<String> loginNameList = Collections.singletonList(userId);
            Result<List<SysUserDTO>> resultUser = userApi.getUserByLoginName(loginNameList);
            if (!resultUser.isSucc()) {
                log.warn("[密码同步] 用户不存在 | appId={} | userId={}", appId, userId);
                return Result.ssoResponse(SsoResultCode.USER_NOT_EXISTS);
            }
            List<SysUserDTO> userList = resultUser.getData();
            if (userList == null || userList.isEmpty()) {
                log.warn("[密码同步] 用户不存在 | appId={} | userId={}", appId, userId);
                return Result.ssoResponse(SsoResultCode.USER_NOT_EXISTS);
            }
            Long userPkId = userList.get(0).getUserId();
            //解密密码
            log.info("[密码同步] 开始解密密码 | appId={} | userId={}", appId, userId);
            String rsaPassWord = ssoService.getRsaPassWord(password);
            log.info("[密码同步] 密码解密成功 | appId={} | userId={} | password(解密后)={}", appId, userId, rsaPassWord);
            //推送修改密码
            log.info("[密码同步] 开始推送密码修改 | appId={} | userId={}", appId, userId);
                Result updateResult = authUserApi.updatePwdByUserId(String.valueOf(userPkId), rsaPassWord);
            if (!updateResult.isSucc()) {
                log.error("[密码同步] 密码修改失败 | appId={} | userId={} | result={}", appId, userId, updateResult);
                throw new SunyardException("密码修改失败");
            }
            log.info("[密码同步] 密码修改成功 | appId={} | userId={}", appId, userId);

        } catch (SunyardException e) {
            log.error("[密码同步] 密码解密或业务异常 | appId={} | userId={} | error={}", appId, userId, e.getMessage());
            return Result.ssoResponse(SsoResultCode.PASSWORD_POLICY_VIOLATION);
        } catch (Exception e) {
            log.error("[密码同步] 系统异常 | appId={} | userId={}", appId, userId, e);
            return Result.ssoResponse(SsoResultCode.SYSTEM_ERROR);
        }
        return Result.ssoResponse(SsoResultCode.SUCCESS);
    }


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

    private Map getLoginResult(AccountToken accountToken) {
        Map<String, Object> map = new HashMap<>(10);
        map.put("pwdSuggest",0);
        Result<SysUserDTO> userDetail = userApi.getUserDetail(accountToken.getUsername());
        if (userDetail.isSucc() && userDetail.getData() != null) {
            SysUserDTO user = userDetail.getData();
            user.setPwd(null);
            user.setSalt(null);
            user.setIsDeleted(null);
            map.put("user", user);
        }
//        map.put("user", authOrgApi.selectOrg(getToken().getId()).getData());
        map.put("AuthorizationToken", SecurityUtils.getSubject().getSession().getId().toString());
        // 添加用户类型
        map.put("loginType", accountToken.getLoginType());
        return map;
    }

}
