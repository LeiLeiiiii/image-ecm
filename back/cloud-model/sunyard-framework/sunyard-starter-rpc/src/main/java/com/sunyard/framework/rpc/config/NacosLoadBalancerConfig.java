package com.sunyard.framework.rpc.config;
/*
 * Project: Sunyard
 *
 * File Created at 2023/5/23
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.sunyard.framework.rpc.dev.CustomNacosLoadBalancer;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/23 15:33
 */
@Configuration(proxyBeanMethods = false)
@LoadBalancerClients(defaultConfiguration = NacosLoadBalancerConfig.class)
public class NacosLoadBalancerConfig {
    /**
     *
     * @param environment
     * @param loadBalancerClientFactory
     * @param nacosDiscoveryProperties
     * @return
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> nacosLoadBalancer(Environment environment,
        LoadBalancerClientFactory loadBalancerClientFactory, NacosDiscoveryProperties nacosDiscoveryProperties) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new CustomNacosLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name,
            nacosDiscoveryProperties);
    }
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/23 Leo creat
 */