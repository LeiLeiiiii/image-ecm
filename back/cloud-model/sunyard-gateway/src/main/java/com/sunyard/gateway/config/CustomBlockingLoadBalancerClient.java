package com.sunyard.gateway.config;

import java.util.concurrent.CompletableFuture;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;

import reactor.core.publisher.Mono;

/**
 * @Description: 自定义CustomBlockingLoadBalancerClient.java
 * @Date: 2023/9/20 16:16
 */
public class CustomBlockingLoadBalancerClient extends BlockingLoadBalancerClient {
    private final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerClientFactory;

    public CustomBlockingLoadBalancerClient(LoadBalancerClientFactory loadBalancerClientFactory, LoadBalancerProperties properties) {
        super(loadBalancerClientFactory, properties);
        this.loadBalancerClientFactory = loadBalancerClientFactory;
    }

    @Override
    public <T> ServiceInstance choose(String serviceId, Request<T> request) {
        ReactiveLoadBalancer<ServiceInstance> loadBalancer = loadBalancerClientFactory.getInstance(serviceId);
        if (loadBalancer == null) {
            return null;
        }
        CompletableFuture<Response<ServiceInstance>> f = CompletableFuture.supplyAsync(() -> {
            Response<ServiceInstance> loadBalancerResponse = Mono.from(loadBalancer.choose(request)).block();
            return loadBalancerResponse;
        });
        Response<ServiceInstance> loadBalancerResponse = null;


        try {
            loadBalancerResponse = f.get();
        } catch (InterruptedException e) {
            
        } catch (Exception e) {
            
        }
        if (loadBalancerResponse == null) {
            return null;
        }
        return loadBalancerResponse.getServer();
    }
}
