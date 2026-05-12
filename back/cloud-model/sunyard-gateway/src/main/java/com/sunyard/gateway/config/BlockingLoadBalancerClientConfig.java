package com.sunyard.gateway.config;

import javax.annotation.Resource;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: BlockingLoadBalancerClient配置类,创建自定义的BlockingLoadBalancerClient Bean
 * @Date: 2023/9/20 16:31
 */
@Configuration
public class BlockingLoadBalancerClientConfig {

    @Resource
    private LoadBalancerClientFactory loadBalancerClientFactory;

    @Resource
    private LoadBalancerProperties properties;

    /**
     *
     * */
    @Bean
    public LoadBalancerClient blockingLoadBalancerClient() {
        return new CustomBlockingLoadBalancerClient(loadBalancerClientFactory, properties);
    }
}
