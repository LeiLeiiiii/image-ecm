package com.sunyard.gateway.config;

import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * @author P-JWei
 * @date 2024/5/20 16:59:32
 * @title
 * @description
 */
@Configuration
public class InitBeanConfig {
    /**
     * Spring-GateWay使用的WebFlux架构。 HttpMessage转换器，用于对Feign的请求转换。
     *
     * @param converters converters
     * @return Result
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }


    /**
     * Gateway默认使用基于Netty容器的RequestUpgradeStrategy和WebSocketClient导致报错，声明Tomcat容器对应的Bean来覆盖它可以解决这个问题
     * @return Result
     */
    // @Bean
    // @Primary
    // public RequestUpgradeStrategy requestUpgradeStrategy() {
    // return new TomcatRequestUpgradeStrategy();
    // }

    /**
     *
     * @return Result
     */
    // @Bean
    // @Primary
    // public WebSocketClient webSocketClient() {
    // return new TomcatWebSocketClient();
    // }
}
