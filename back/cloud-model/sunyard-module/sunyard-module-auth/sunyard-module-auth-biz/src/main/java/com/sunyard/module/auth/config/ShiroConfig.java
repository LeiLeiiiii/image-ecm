package com.sunyard.module.auth.config;
/*
 * Project: com.sunyard.am.config
 *
 * File Created at 2021/7/2
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.Filter;

import com.sunyard.module.auth.constant.CachePrefixConstants;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import com.sunyard.module.auth.config.properties.AuthProperties;
import com.sunyard.module.auth.constant.TokenConstants;
import com.sunyard.module.auth.shiro.JsonSessionFactory;
import com.sunyard.module.auth.shiro.LoginAuthenticator;
import com.sunyard.module.auth.shiro.RedisCacheManager;
import com.sunyard.module.auth.shiro.RedisSessionDAO;
import com.sunyard.module.auth.shiro.ShiroSessionManager;
import com.sunyard.module.auth.shiro.filter.AuthcFilter;
import com.sunyard.module.auth.shiro.filter.RolesFilter;
import com.sunyard.module.auth.shiro.matcher.PasswordMatcher;
import com.sunyard.module.auth.shiro.realm.LdapRealm;
import com.sunyard.module.auth.shiro.realm.LoaclRealm;
import com.sunyard.module.auth.shiro.realm.SsoRealm;
import com.sunyard.module.auth.shiro.realm.SuperRealm;
import com.sunyard.module.system.api.ApiAuthApi;
import com.sunyard.module.system.api.AuthShiroApi;
import com.sunyard.module.system.api.dto.SysMenuDTO;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.shiro
 * @Desc
 * @date 2021/7/2 13:33
 */
@Configuration
public class ShiroConfig {
    @Resource(name = "shiroRedisTemplate")
    private RedisTemplate<String, Session> shiroRedisTemplate;
    @Resource
    private PasswordMatcher passwordMatcher;
    @Resource
    private AuthShiroApi authShiroApi;
    @Resource
    private ApiAuthApi apiAuthApi;
    @Resource
    private AuthProperties authProperties;
    /**
     *
     */
    @Bean(name = "lifecycleBeanPostProcessor")
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * redisCacheManager
     *
     * @return Result
     */
    @Bean
    public RedisCacheManager redisCacheManager() {
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisTemplate(shiroRedisTemplate);
        redisCacheManager.setExpire(authProperties.getSessionIdExpire());
        redisCacheManager.setKeyPrefix(CachePrefixConstants.AUTH);
        return redisCacheManager;
    }

    /***/
    @Bean
    public RedisSessionDAO redisSessionDAO() {
        RedisSessionDAO sessionDAO = new RedisSessionDAO();
        sessionDAO.setCacheManager(redisCacheManager());
        return sessionDAO;
    }

    /**
     * 本地登录
     */
    @Bean
    public LoaclRealm loaclRealm() {
        LoaclRealm realm = new LoaclRealm();
        realm.setCachingEnabled(true);
        realm.setAuthenticationCachingEnabled(true);
        realm.setAuthorizationCachingEnabled(true);
        realm.setCredentialsMatcher(passwordMatcher);
        // 配置后redisCacheManager 用户信息会和session的缓存分开存放并且退登后不会清除
        /* accountRealm.setCacheManager(redisCacheManager());*/
        return realm;
    }

    /**
     * 系统超管
     */
    @Bean
    public SuperRealm superRealm() {
        SuperRealm realm = new SuperRealm();
        realm.setCachingEnabled(true);
        realm.setAuthenticationCachingEnabled(true);
        realm.setAuthorizationCachingEnabled(true);
        realm.setCredentialsMatcher(passwordMatcher);
        return realm;
    }

    /***/
    @Bean
    public LdapRealm ldapRealm() {
        LdapRealm realm = new LdapRealm();
        realm.setCachingEnabled(true);
        realm.setAuthenticationCachingEnabled(true);
        realm.setAuthorizationCachingEnabled(true);
        realm.setCredentialsMatcher(passwordMatcher);
        return realm;
    }
    /***/
    @Bean
    public SsoRealm ssoRealm() {
        SsoRealm realm = new SsoRealm();
        realm.setCachingEnabled(true);
        realm.setAuthenticationCachingEnabled(true);
        realm.setAuthorizationCachingEnabled(true);
        realm.setCredentialsMatcher(passwordMatcher);
        return realm;
    }
    private List<Realm> realms() {
        List<Realm> realms = new ArrayList<>();
        realms.add(loaclRealm());
        realms.add(superRealm());
        realms.add(ldapRealm());
        realms.add(ssoRealm());
        return realms;
    }

    /**
     * 配置使用自定义认证器，可以实现多Realm认证，并且可以指定特定Realm处理特定类型的验证
     */
    @Bean(name = "loginAuthenticator")
    public LoginAuthenticator loginAuthenticator() {
        LoginAuthenticator authenticator = new LoginAuthenticator();
        // 配置认证策略，只要有一个Realm认证成功即可，并且返回所有认证成功信息
        authenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
        authenticator.setRealms(realms());
        return authenticator;
    }

    /**
     * DefaultAdvisorAutoProxyCreator是用来扫描上下文，寻找所有的Advistor(通知器），
     * 将这些Advisor应用到所有符合切入点的Bean中。所以必须在lifecycleBeanPostProcessor创建之后创建
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }

    /**
     * Shiro权限注解， 但必须在配置了LifecycleBeanPostProcessor 之后才可以使用
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor
    authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor =
                new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * 配置SecurityManager的管理
     */
    @Bean(name = "securityManager")
    public DefaultWebSecurityManager securityManager(LoginAuthenticator loginAuthenticator) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 配置需要使用的Realms 授权doGetAuthenticationInfo 必配
        securityManager.setAuthenticator(loginAuthenticator);
        securityManager.setRealms(realms());
        // 定义要使用的session管理器
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }

    /**
     * sessionIdCookie的实现,用于重写覆盖容器默认的JSESSIONID
     *
     * @Bean
     */
    public SimpleCookie simpleCookie() {
        SimpleCookie simpleCookie = new SimpleCookie();
        // 设置Cookie名字, 默认为: JSESSIONID 问题: 与SERVLET容器名冲突, 如JETTY, TOMCAT 等默认JSESSIONID,
        // 当跳出SHIRO SERVLET时如ERROR-PAGE容器会为JSESSIONID重新分配值导致登录会话丢失!
        simpleCookie.setName(TokenConstants.SUNYARD_TOKEN);
        simpleCookie.setHttpOnly(true);
        // maxAge=-1表示浏览器关闭时失效此Cookie
        simpleCookie.setMaxAge(-1);
        return simpleCookie;
    }

    /**
     *
     */
    @Bean
    public JsonSessionFactory jsonSessionFactory() {
        return new JsonSessionFactory();
    }

    /**
     * 定义会话管理器的操作
     */
    @Bean(name = "sessionManager")
    public SessionManager sessionManager() {
        ShiroSessionManager sessionManager = new ShiroSessionManager();
        sessionManager.setSessionFactory(jsonSessionFactory());
        // 会话超时时间，单位：毫秒
        sessionManager.setGlobalSessionTimeout(authProperties.getSessionIdExpire() * 1000);
        // 是否开启定时清理失效会话
        sessionManager.setSessionValidationSchedulerEnabled(true);
        // 定时清理失效会话, 清理用户直接关闭浏览器造成的孤立会话
        sessionManager.setSessionValidationInterval(60 * 1000);
        // 删除所有无效的Session对象，此时的session被保存在了内存里面
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionIdCookie(simpleCookie());
        // 是否允许将sessionId 放到 cookie 中
        // 此注释代码 就是将JSESSIONID变成自定义名称 WEBJSESSIONID
        sessionManager.setSessionIdCookieEnabled(true);
        // 去掉URL中的JSESSIONID
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        // 设置后session 进入缓存，重启应用后维持
        sessionManager.setCacheManager(redisCacheManager());
        // 自定义sessiondao
        sessionManager.setSessionDAO(redisSessionDAO());
        return sessionManager;
    }

    /**
     * 加载shiroFilter权限控制规则
     */
    @Bean
    public Map<String, String> filterChainDefinitionMap() {
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // actuator
        filterChainDefinitionMap.put("/actuator/**", "anon");
        // druid admin
        filterChainDefinitionMap.put("/druid/**", "anon");
        // swagger接⼝权限开放
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/swagger-ui/*", "anon");
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/v3/**", "anon");
        // 登录
        filterChainDefinitionMap.put("/account/**", "anon");
        filterChainDefinitionMap.put("/sso/**", "anon");
        // 因对外api需跳过shiro认证，所以得初始化
        filterChainDefinitionMap.put("/oauth/**", "anon");
        filterChainDefinitionMap.put("/getOutApiToken", "anon");
        filterChainDefinitionMap.put("/getRefreshApiToken", "anon");
        filterChainDefinitionMap.put("/getPrivateKey", "anon");
        filterChainDefinitionMap.put("/rpc-api/**", "anon");
        // 对外api跳过shiro认证
        Map<String, String> map = apiAuthApi.getAllApiUrl(0).getData();
        map.forEach((key, value) -> filterChainDefinitionMap.put(value, "anon"));
        // 从数据库获取
        List<SysMenuDTO> list = authShiroApi.searchShiroPaths().getData();
        for (SysMenuDTO role : list) {
            if (StringUtils.hasText(role.getPath()) && StringUtils.hasText(role.getPerms())) {
                filterChainDefinitionMap.put(role.getPath(), "roles[" + role.getPerms() + "]");
            }
        }
        filterChainDefinitionMap.put("/**", "authc");
        return filterChainDefinitionMap;
    }

    /**
     * filters
     */
    @Bean
    public Map<String, Filter> filters() {
        Map<String, Filter> filters = new HashMap<>(6);
        filters.put("authc", new AuthcFilter());
        filters.put("roles", new RolesFilter());
        return filters;
    }

    /**
     * 配置ShiroFilter
     */
    @Bean(name = "shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 配置拦截器,实现无权限返回401,而不是跳转到登录页
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        // loginUrl 为登录页面地址
        shiroFilterFactoryBean.setLoginUrl("/login");
        // successUrl 为登录成功页面地址
        shiroFilterFactoryBean.setSuccessUrl("/");
        // 认证未通过访问的页面
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauthorizedUrl");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap());
        shiroFilterFactoryBean.setFilters(filters());
        return shiroFilterFactoryBean;
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/2 zhouleibin creat
 */
