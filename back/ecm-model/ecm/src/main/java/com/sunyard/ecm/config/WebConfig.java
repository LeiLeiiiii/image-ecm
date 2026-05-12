package com.sunyard.ecm.config;

import com.sunyard.ecm.interceptor.OpenApiInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @author 饶昌妹
 * @Desc 拦截器配置类
 * @date 2023/6/13 14:51
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 拦截路径配置
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new OpenApiInterceptor())
                .addPathPatterns("/api/ecms/open/**");
    }

}


