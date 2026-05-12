package com.sunyard.framework.rpc.util;

import org.springframework.beans.BeansException;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author P-JWei
 * @date 2023/5/24 15:28 @title：
 * @description:
 */
@Component
public class OpenFeignBuilderUtils implements ApplicationContextAware {

    private static FeignClientBuilder builder;

    /**
     * 手动生成FeignClient,准备一个FeignClient基类，该类不用打{@link org.springframework.cloud.openfeign.FeignClient}注解
     *
     * @param clazz feignClient基类
     * @param name  服务名
     * @param <T>   T
     * @return Result
     */
    public static <T> T getFeignClient(Class<T> clazz, String name) {
        FeignClientBuilder.Builder<T> feignClientBuilder = builder.forType(clazz, name);
        return feignClientBuilder.build();
    }

    /**
     * 通过spring applicationContext生成builder
     *
     * @param applicationContext 实现ApplicationContextAware接口回调注入app
     * @throws BeansException 异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        builder = new FeignClientBuilder(applicationContext);
    }
}
