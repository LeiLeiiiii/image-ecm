package com.sunyard.module.auth.shiro;
/*
 * Project: com.sunyard.am.shiro
 *
 * File Created at 2021/7/2
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.sunyard.framework.common.token.AccountToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.shiro
 * @Desc * 自定义Authenticator 注意，当需要分别定义处理普通用户和管理员验证的Realm时，对应Realm的全类名应该包含字符串“User”，或者“Admin”。
 *       并且，他们不能相互包含，例如，处理普通用户验证的Realm的全类名中不应该包含字符串"Admin"。
 * @date 2021/7/2 14:12
 */

public class LoginAuthenticator extends ModularRealmAuthenticator {
    /**
     * 判断是单Realm还是多Realm
     *
     * @param authenticationToken authenticationToken
     * @return Result AuthenticationInfo
     */
    @Override
    public AuthenticationInfo doAuthenticate(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 判断getRealms()是否返回为空
        assertRealmsConfigured();
        AccountToken loginToken = (AccountToken)authenticationToken;
        // 所有Realm
        Collection<Realm> realms = getRealms();
        // 登录类型对应的所有Realm
        String realmType = loginToken.getRealmType();
        Collection<Realm> typeRealms = new ArrayList<Realm>();
        for (Realm realm : realms) {
            if (realm.getName().contains(realmType)) {
                typeRealms.add(realm);
            }
        }

        // 判断是单Realm还是多Realm
        if (typeRealms.size() > 1) {
            return doMultiRealmAuthentication(typeRealms, loginToken);
        } else {
            return doSingleRealmAuthentication(typeRealms.iterator().next(), loginToken);
        }
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/2 zhouleibin creat
 */
