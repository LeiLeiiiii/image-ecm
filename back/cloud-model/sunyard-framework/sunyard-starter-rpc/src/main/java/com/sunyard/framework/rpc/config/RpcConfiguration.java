package com.sunyard.framework.rpc.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhouleibin
 */
@Configuration(proxyBeanMethods = false)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.sunyard"})
@ComponentScan(basePackages = "com.sunyard.framework.rpc")
public class RpcConfiguration {}
