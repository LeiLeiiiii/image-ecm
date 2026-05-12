package com.sunyard.module.auth.shiro.realm;
/*
 * Project: com.sunyard.am.shiro.realm
 *
 * File Created at 2021/7/2
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
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
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.util.ObjectUtils;

import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.module.system.api.AuthUserApi;
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.shiro.realm
 * @Desc
 * @date 2021/7/2 12:47
 */
public class LoaclRealm extends AuthorizingRealm {

    @Resource
    private AuthUserApi authUserApi;
    @Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
        throws AuthenticationException {
        AccountToken loginToken = (AccountToken)authenticationToken;
        List<SysUserDTO> list = authUserApi.search(loginToken.getUsername()).getData();
        if (ObjectUtils.isEmpty(list)) {
            throw new UnknownAccountException("登录名或密码错误，请重新输入!");
        }
        SysUserDTO user = list.get(0);
        loginToken.setId(user.getUserId());
        loginToken.setName(user.getName());
        loginToken.setDeptId(user.getDeptId());
        loginToken.setInstId(user.getInstId());
        loginToken.setOrgName(user.getInstName());
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
        SessionsSecurityManager securityManager = (SessionsSecurityManager)SecurityUtils.getSecurityManager();
        DefaultWebSessionManager sessionManager = (DefaultWebSessionManager)securityManager.getSessionManager();
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            Collection<Session> sessions = sessionManager.getSessionDAO().getActiveSessions();
            for (Session session : sessions) {
                if (null != session && null != session.getAttribute("user")) {
                    String sessionUserId = (String)session.getAttribute("user");
                    String loginTokenUserId = loginToken.getId().toString();
                    // 如果是登录进来的，则踢出已在线用户
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
            AccountToken loginToken = (AccountToken)SecurityUtils.getSubject().getPrincipal();
            Long userId = loginToken.getId();
            List<SysMenuDTO> list = authUserApi.getRoleByUserId(userId).getData();
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
 * Date Author Note ------------------------------------------------------------------------- 2021/7/2 zhouleibin creat
 */
