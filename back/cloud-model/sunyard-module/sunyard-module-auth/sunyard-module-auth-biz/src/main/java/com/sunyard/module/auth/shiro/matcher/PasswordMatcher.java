package com.sunyard.module.auth.shiro.matcher;
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

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.codec.CodecSupport;
import org.springframework.stereotype.Component;

import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.PasswordUtils;

/**
 * @author zhouleibin
 * @Type
 * @Desc
 * @date 2021/7/14 12:43
 */
@Component
public class PasswordMatcher extends SimpleCredentialsMatcher {
    /**
     * 先实现一个接口SimpleCredentialsMatcher 获取doCredentialsMatch的内部方法
     *
     * @param token AuthenticationToken 本身并不直接存储在数据库中，而是用于封装用户在登录时提供的凭证（如用户名和密码）
     * @param info  AuthenticationInfo 包含了用户的身份信息和凭证信息，这些信息通常从数据库中获取
     * @return Result
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        AccountToken submitToken = (AccountToken) token;
        
        // SSO 登录不需要密码校验
        if ("SsoRealm".equals(submitToken.getRealmType())) {
            return true;
        }
        
        SimpleAuthenticationInfo storedInfo=(SimpleAuthenticationInfo) info;

        String storedSalt= CodecSupport.toString((storedInfo.getCredentialsSalt().getBytes()));
        String submitPassword = PasswordUtils.getEncryptionPassword(storedSalt,new String(submitToken.getPassword()));
        String storedPassword = (String) info.getCredentials();
        return super.equals(submitPassword, storedPassword);
    }
}
