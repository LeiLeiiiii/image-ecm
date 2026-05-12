package com.sunyard.module.auth.oauth.config;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import com.sunyard.module.auth.config.properties.AuthOauth2Properties;

/**
 * @author P-JWei
 * @date 2023/7/26 10:33:44
 * @title 认证授权服务
 * @description 用于初始化客户端、token时效
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    /**
     * webSecurityConfig 中配置的AuthenticationManager
     */
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private RedisTokenStore redisTokenStore;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private AuthOauth2Properties authOauth2Properties;

    /**
     * 用来配置客户端详情服务（ClientDetailsService），客户端详情信息在 这里进行初始化 ， 你能够把客户端详情信息写死在这里或者是通过数据库来存储调取详情信息。
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory().withClient(authOauth2Properties.getClientId())// 客户端标识ID
                .secret(passwordEncoder.encode(authOauth2Properties.getClientSecret()))// 客户端安全码
                .authorizedGrantTypes("password", "refresh_token")// 授权类型:密码模式
                .scopes("all");// 客户端访问范围
    }

    /**
     * 用来配置令牌端点的安全约束
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                // 允许访问token的公钥，默认/oauth/token_key 是受保护的
                .tokenKeyAccess("permitAll()")
                // 允许检查token状态，默认/oauth/check_token 是受保护的
                .checkTokenAccess("permitAll()")
                // 允许表单登录
                .allowFormAuthenticationForClients();
    }

    /**
     * 用来配置令牌（token）的访问端点和令牌服务(token services)
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager);// 认证管理器
        endpoints.tokenServices(tokenServices());// 令牌管理服务
        endpoints.allowedTokenEndpointRequestMethods(HttpMethod.POST, HttpMethod.GET);// 接收GET和POST

    }

    /**
     * DefaultTokenService作为oauth2中操作token(crud)的默认实现
     *
     * @return Result
     */
    @Bean
    public DefaultTokenServices tokenServices() {
        // 配置TokenServices参数
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        // 支持刷新令牌
        tokenServices.setSupportRefreshToken(true);
        // 即是否在刷新access_token同时刷新refresh_token
        tokenServices.setReuseRefreshToken(true);
        // token存储方式:RedisTokenStore
        tokenServices.setTokenStore(redisTokenStore);
        // 令牌默认有效期1分钟，必须大于续期的时间，最好是一倍
        tokenServices.setAccessTokenValiditySeconds((int) TimeUnit.MINUTES.toSeconds(60));
        // 刷新令牌默认有效期30分钟
        tokenServices.setRefreshTokenValiditySeconds((int) TimeUnit.HOURS.toSeconds(12));
        return tokenServices;
    }

}
