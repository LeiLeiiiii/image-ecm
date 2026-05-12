package com.sunyard.module.auth.oauth.config;

import com.sunyard.module.auth.oauth.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;

/**
 * @author P-JWei
 * @date 2023/7/26 10:33:44
 * @title 认证管理器
 * @description 用于配置认证逻辑
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * userDetailsService 获取token的时候对用户进行一些自定义过滤，并将保存用户信息（用户名，密码，角色等）
     */
    @Resource
    private UserDetailsServiceImpl userDetailsServiceImpl;

    /**
     * 使用MD5对client_secreat进行加密，可以使用默认的加密方式也可以自定义，这里使用MD5加密方式
     * 
     * @return Result PasswordEncoder
     */
    @Resource
    private PasswordEncoder passwordEncoder;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 配置用户签名服务 主要是user-details 机制，
     *
     * @param auth 签名管理器构造器，用于构建用户具体权限控制
     * @throws Exception 异常
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // db方式鉴权 username
        auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
            // controller请求接口
            .antMatchers("/oauth/**", "/account/*", "/getOutApiToken").permitAll()
            // 心跳及内部rpc
            .antMatchers("/actuator/**", "/rpc-api/**").permitAll()
            // 其余需要登录
            .anyRequest().authenticated();

    }
}
