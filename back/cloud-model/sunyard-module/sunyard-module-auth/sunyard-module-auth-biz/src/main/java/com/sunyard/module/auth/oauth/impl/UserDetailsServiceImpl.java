package com.sunyard.module.auth.oauth.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sunyard.framework.common.util.encryption.Md5Utils;
import com.sunyard.module.auth.oauth.dto.CustomUser;
import com.sunyard.module.system.api.ApiAuthApi;
import com.sunyard.module.system.api.dto.SysApiSystemDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author P-JWei
 * @date 2023/7/17 10:20
 * @title 用户信息生成类
 * @description 用户生成UserDetails对象，用于OAuth来认证和鉴权
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private ApiAuthApi apiAuthApi;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //生成UserDetails 对象
        SysApiSystemDTO data = apiAuthApi.getAppObjByAppId(username).getData();
        //注入app所拥有的权限
        List<String> permission = apiAuthApi.getApiCodeByAppId(username).getData();
        String appPermissionString = StringUtils.join(permission.toArray(), ",");
        return new CustomUser(username, Md5Utils.md5Hex(data.getAppSecret()),
                AuthorityUtils.commaSeparatedStringToAuthorityList(appPermissionString),
                data.getSystemReferer(), data.getPublicKey(), data.getPrivateKey());
    }
}
