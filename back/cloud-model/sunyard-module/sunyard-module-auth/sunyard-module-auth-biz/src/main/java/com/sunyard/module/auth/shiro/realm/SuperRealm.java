package com.sunyard.module.auth.shiro.realm;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.util.ObjectUtils;

import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.module.system.api.AuthUserApi;
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.api.dto.SysUserAdminDTO;

/**
 * @author zhouleibin
 * @date 2022/2/7 16:18
 * @Desc 系统超管
 */
public class SuperRealm extends AuthorizingRealm {

    @Resource
    private AuthUserApi authUserApi;
    @Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
        throws AuthenticationException {
        AccountToken loginToken = (AccountToken)authenticationToken;
        List<SysUserAdminDTO> list = authUserApi.adminInfoSearch(loginToken.getUsername()).getData();
        if (ObjectUtils.isEmpty(list)) {
            throw new UnknownAccountException("用户不存在");
        }
        SysUserAdminDTO user = list.get(0);
        loginToken.setId(user.getUserId());
        loginToken.setName(user.getName());
        loginToken.setDeptId(user.getDeptId());
        loginToken.setInstId(user.getInstId());
        // 超级管理员 999
        loginToken.setLoginType(user.getType());
        loginToken.setUpdateTime(user.getUpdateTime());
        loginToken.setPwdUpdateTime(user.getPwdUpdateTime());
        switch (user.getState()) {
            case 0:
                throw new DisabledAccountException("账户已禁用");
            case 2:
                throw new DisabledAccountException("账户已注销");
            case 3:
                throw new LockedAccountException("账户已锁定");
            default:
        }

        // 以下为只允许同一账户单个登录
        DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager)SecurityUtils.getSecurityManager();
        DefaultWebSessionManager sessionManager = (DefaultWebSessionManager)securityManager.getSessionManager();
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            Collection<Session> sessions = sessionManager.getSessionDAO().getActiveSessions();
            for (Session session : sessions) {
                if (null != session && null != session.getAttribute("user")) {
                    String sessionUserId = (String)session.getAttribute("user");
                    String loginTokenUserId = loginToken.getId().toString();
                    // 方法二、当第二次登录时，把第一个session剔除
                    if (loginTokenUserId.equals(sessionUserId)) {
                        sessionManager.getSessionDAO().delete(session);
                        session.setTimeout(0);
                    }
                }
            }
        } else {
            subject.logout();
        }

        // 账号密码校验
        SimpleAuthenticationInfo authcInfo = new SimpleAuthenticationInfo(loginToken,
                user.getPwd(),
                ByteSource.Util.bytes(user.getSalt()),getName());
        Session session = SecurityUtils.getSubject().getSession();
        session.setAttribute("user", loginToken.getId().toString());
        return authcInfo;
    }

    @Override
    public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        for (String realmName : principals.getRealmNames()) {
            if (!realmName.contains(this.getClass().getSimpleName())) {
                continue;
            }
            List<SysMenuDTO> list = authUserApi.getRoleByUserId(null).getData();
            // 获取用户的菜单权限数据
            Set<String> roleSet = new HashSet<String>();
            for (SysMenuDTO menu : list) {
                roleSet.add(menu.getPerms());
            }
            info.setRoles(roleSet);
        }
        return info;
    }
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2022/2/7 zhouleibin creat
 */
