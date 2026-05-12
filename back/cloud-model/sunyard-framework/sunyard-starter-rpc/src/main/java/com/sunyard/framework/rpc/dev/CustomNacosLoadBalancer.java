package com.sunyard.framework.rpc.dev;
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

/**
 * @author Leo
 * @Desc
 * @date 2023/5/23 15:31
 */

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.util.CollectionUtils;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.balancer.NacosBalancer;
import com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer;
import com.sunyard.framework.common.util.ip.IpUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 修改Nacos的负载均衡策略，主要是为了将请求路由到本地服务中，方便开发
 * 
 * @author guojunwang
 * @date 2022-05-18 14:17
 */
@Slf4j
public class CustomNacosLoadBalancer extends NacosLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    public CustomNacosLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
        String serviceId, NacosDiscoveryProperties nacosDiscoveryProperties) {
        super(serviceInstanceListSupplierProvider, serviceId, nacosDiscoveryProperties);
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier =
            serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        return supplier.get().next().map(this::getInstanceResponse);
    }

    /**
     * 获取实例的响应服务
     * @param serviceInstances serviceInstances
     * @return Response
     */
    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> serviceInstances) {
        if (serviceInstances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }

        try {
            String clusterName = this.nacosDiscoveryProperties.getClusterName();

            List<ServiceInstance> instancesToChoose = serviceInstances;

            if (StringUtils.isNotBlank(clusterName)) {
                List<ServiceInstance> sameClusterInstances = serviceInstances.stream().filter(serviceInstance -> {
                    String cluster = serviceInstance.getMetadata().get("nacos.cluster");
                    return StringUtils.equals(cluster, clusterName);
                }).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(sameClusterInstances)) {
                    instancesToChoose = sameClusterInstances;
                }
            } else {
                log.warn("A cross-cluster call occurs，name = {}, clusterName = {}, instance = {}", serviceId,
                    clusterName, serviceInstances);
            }

            // 本地开发模式路由会本机
            if (StrUtil.equals(SpringUtil.getProperty("spring.profiles.active"), "local")) {

                // 如果有本机的服务，优先选择本地服务
                List<ServiceInstance> localServiceInstance =
                    instancesToChoose.stream().filter(i -> IpUtils.isLocal(i.getHost())).collect(Collectors.toList());
                // 使用本地服务进行选举
                if (CollUtil.isNotEmpty(localServiceInstance)) {
                    instancesToChoose = localServiceInstance;
                    log.trace("开发模式，负载均衡器优先选择本机服务调用==>{}", instancesToChoose.stream().map(is -> {
                        String msg = " serviceId=%s,host=%s,port=%s ";
                        return String.format(msg, is.getServiceId(), is.getHost(), is.getPort());
                    }).collect(Collectors.toList()));
                }
            }

            // 权重选取
            ServiceInstance instance = NacosBalancer.getHostByRandomWeight3(instancesToChoose);
            return new DefaultResponse(instance);
        } catch (Exception e) {
            log.warn("NacosLoadBalancer error", e);
            return null;
        }

    }

}

/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/23 Leo creat
 */