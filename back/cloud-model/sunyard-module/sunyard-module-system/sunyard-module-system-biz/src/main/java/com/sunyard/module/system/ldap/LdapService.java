package com.sunyard.module.system.ldap;

import javax.annotation.Resource;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.conversion.JsonUtils;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.system.constant.CachePrefixConstants;
import com.sunyard.module.system.ldap.util.LdapUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @Desc
 * @date 10:34 2021/11/19
 */
@Slf4j
@Component
public class LdapService {

    @Resource
    private LdapTemplate ldapTemplate;
    @Resource
    private RedisUtils redisUtils;

    /**
     * 登录认证使用
     * @param loginName 登录名
     * @param pw 密码
     * @return result
     */
    public Result login(String loginName, String pw) {
        String ldapConfigStr = redisUtils.get(CachePrefixConstants.SYSTEM + "ldap");
        if (!StringUtils.hasText(ldapConfigStr)) {

            throw new RuntimeException("ldap配置错误，请重新配置保存");
        }
        JSONObject ldapConfig = JsonUtils.parseObject(ldapConfigStr);
        String url = ldapConfig.getString("url");
        String base = ldapConfig.getString("base");
        String userDn = ldapConfig.getString("userDn");
        String password = ldapConfig.getString("password");
        return LdapUtils.authenticate1(url, base, userDn, password, loginName, pw);
    }

    /**
     * 测试连通使用
     * @param url url
     * @param base base
     * @param loginName 登录名
     * @param pw 密码
     * @return result
     */
    public Result authenticate(String url, String base, String loginName, String pw) {
        String userDn = loginName;
        String password = pw;
        return LdapUtils.authenticate1(url, base, userDn, password, loginName, pw);
    }
}
