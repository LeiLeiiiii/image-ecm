package com.sunyard.sunafm.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author l2
 * @Description 定义异步任务执行线程池配置类
 * @since 2024/2/22 10:56
 */
@Slf4j
@EnableAsync
@Configuration
public class ThreadPoolConfig {
    @Value("${sunyard.corePoolSize}")
    private Integer corePoolSize;
    @Value("${sunyard.maxPoolSize}")
    private Integer maxPoolSize;
    @Value("${sunyard.queueCapacity}")
    private Integer queueCapacity;
    @Value("${sunyard.keepAliveSeconds}")
    private Integer keepAliveSeconds;
    @Value("${sunyard.awaitTerminationSeconds}")
    private Integer awaitTerminationSeconds;

    /**
     * 1.setCorePoolSize(10)      核心线程数10：线程池创建时候初始化的线程数
     * 2.setMaxPoolSize(15)       最大线程数20：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
     * 3.setQueueCapacity(200)    缓冲队列200：用来缓冲执行任务的队列
     * 4.setKeepAliveSeconds(60)  允许线程的空闲时间60秒：当超过了核心线程数之外的线程在空闲时间到达之后会被销毁
     * 5.executor.setThreadNamePrefix("taskExecutor-"); 线程池名的前缀：设置好了之后可以方便定位处理任务所在的线程池
     * 6.RejectedExecutionHandler:  线程池对拒绝任务的处理策略：这里采用了CallerRunsPolicy策略，
     * 当线程池没有处理能力的时候，该策略会直接在 execute 方法的调用线程中运行被拒绝的任务；
     * 如果执行程序已关闭，则会丢弃该任务
     * 7.setWaitForTasksToCompleteOnShutdown(true);    设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean
     * 8.setAwaitTerminationSeconds 设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
     *
     * @return
     */

    @Bean("GlobalThreadPool")
    public Executor globalThreadPool() {
        String threadName = "Global-thread-pool";
        log.info("开始初始化线程池:{}", threadName);
        //获取机器cpu配置
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        if (corePoolSize == null) {
            corePoolSize = 3;
        }
        if (maxPoolSize == null) {
            maxPoolSize = 4;
        }
        if (queueCapacity == null) {
            queueCapacity = 200;
        }
        if (keepAliveSeconds == null) {
            keepAliveSeconds = 60;
        }
        if (awaitTerminationSeconds == null) {
            awaitTerminationSeconds = 600;
        }
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.setThreadNamePrefix(threadName + "-");
        log.info("初始化线程池完成:{},核心线程为:{}.", threadName, executor.getCorePoolSize());
        return executor;
    }

    @Bean(name = "checkRepeatExecutor")
    public ThreadPoolTaskExecutor getCheckRepeatExecutor() {
        String threadName = "CheckRepeatExecutor-";
        log.info("开始初始化线程池:{}", threadName);
        //获取机器cpu配置
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        if (corePoolSize == null) {
            corePoolSize = 3;
        }
        if (maxPoolSize == null) {
            maxPoolSize = 4;
        }
        if (queueCapacity == null) {
            queueCapacity = 200;
        }
        if (keepAliveSeconds == null) {
            keepAliveSeconds = 60;
        }
        if (awaitTerminationSeconds == null) {
            awaitTerminationSeconds = 600;
        }
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.setThreadNamePrefix(threadName + "-");
        log.info("初始化线程池完成:{},核心线程为:{}.", threadName, executor.getCorePoolSize());
        return executor;
    }
}