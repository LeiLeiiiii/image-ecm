package com.sunyard.module.auth.oauth.config;

import com.sunyard.module.system.api.ApiAuthApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author P-JWei
 * @date 2023/7/26 10:33:44
 * @title 资源服务器
 * @description 用户配置资源的访问维度
 */
@Slf4j
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    public static Map<String, String> allApi;

    @Resource
    private ApiAuthApi apiAuthApi;
    @Resource
    private AuthenticationEntryPointConfig authenticationEntryPointConfig;
    @Resource
    private AccessDeniedHandlerConfig accessDeniedHandlerConfig;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        // authenticationEntryPoint 认证异常流程处理返回
        resources.authenticationEntryPoint(authenticationEntryPointConfig)
            .accessDeniedHandler(accessDeniedHandlerConfig);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // 禁用了csrf（跨站请求伪造）功能
        http.csrf().disable()
                // 限定签名成功的请求
            .authorizeRequests()
            // controller请求接口
            .antMatchers("/oauth/**", "/account/*", "/getOutApiToken").permitAll()
            // 心跳及内部rpc
            .antMatchers("/actuator/**", "/rpc-api/**").permitAll();
        injectApi(http);
    }

    private void injectApi(HttpSecurity http) {
        allApi = apiAuthApi.getAllApiUrl(0).getData();
        if (!CollectionUtils.isEmpty(allApi)) {
            allApi.forEach((key, value) -> {
                try {
                    http.authorizeRequests().antMatchers(value).hasAuthority(key);
                } catch (Exception e) {
                    log.error("系统异常", e);
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
