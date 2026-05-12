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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;

import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.module.system.api.AuthUserApi;
import com.sunyard.module.system.api.dto.SysMenuDTO;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.shiro.realm
 * @Desc
 * @date 2021/7/2 12:53
 */
public class SsoRealm extends AuthorizingRealm {

    @Resource
    private AuthUserApi authUserApi;

    @Override
    public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {
        AccountToken loginToken = (AccountToken) authenticationToken;
        
        // SSO 登录不需要密码校验，直接返回认证信息
        SimpleAuthenticationInfo authcInfo = new SimpleAuthenticationInfo(
            loginToken,  // principal - 用户信息
            "",          // credentials - 密码为空
            getName()     // realm 名称
        );
        
        // 设置会话属性
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
            AccountToken loginToken = (AccountToken) SecurityUtils.getSubject().getPrincipal();
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
