package com.sunyard.module.system.ldap.util;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @date 2022/2/7 19:48
 * @Desc
 */
@Slf4j
public class LdapUtils {

    /**
     * ldap认证
     * @param url 认证服务url
     * @param base base
     * @param userDn userDn
     * @param password 密码
     * @param principal principal
     * @param credentials credentials
     * @return Result
     */
    public static Result authenticate1(String url, String base, String userDn, String password, String principal,
        String credentials) {
        LdapContextSource source = new LdapContextSource();
        source.setUrl(url);
        source.setBase(base);
        source.setUserDn(userDn);
        source.setPassword(password);
        source.afterPropertiesSet();
        LdapTemplate ldapTemplate = new LdapTemplate(source);
        ldapTemplate.setIgnorePartialResultException(true);
        EqualsFilter filter = new EqualsFilter("sAMAccountName", principal);
        try {
            boolean result = ldapTemplate.authenticate("", filter.toString(), credentials);
            if (result) {
                return Result.success("登录成功");
            } else {
                return Result.error("登录失败", ResultCode.PARAM_ERROR);
            }
        } catch (org.springframework.ldap.AuthenticationException e) {
            log.error("系统异常",e);
            throw new RuntimeException("AD域身份验证失败!");
        } catch (org.springframework.ldap.CommunicationException e) {
            log.error("系统异常",e);
            throw new RuntimeException("AD域连接失败!");
        } catch (Exception e) {
            log.error("系统异常",e);
            throw new RuntimeException("AD域身份验证未知异常!");
        }

    }

}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2022/2/7 zhouleibin creat
 */
