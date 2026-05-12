package com.sunyard.module.system.config;
/*
 * Project: Sunyard
 *
 * File Created at 2023/6/14
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Leo
 * @Desc
 * @date 2023/6/14 11:06
 */
@Slf4j
@EnableAsync
@Configuration
public class ThreadPoolConfig {

    /**
     * cpu 核心数量
     */
    public static final int CPU_NUM = Runtime.getRuntime().availableProcessors();

    /**
     * @return
     */
    @Bean("GlobalThreadPool")
    public Executor globalThreadPool() {
        String threadName = "Global-thread-pool";
        log.info("开始初始化线程池:{}", threadName);
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 核心线程数：当线程池中的线程数量为 corePoolSize 时，即使这些线程处于空闲状态，也不会销毁（除非设置 allowCoreThreadTimeOut=true）。
        // -> 核心线程，也就是正在处理中的任务
        // -> 虽然 CPU 核心数可以作为线程池中线程数量的参考指标，但最终线程数量还需要根据具体情况进行设置和调整。
        // -> 如果同时运行的线程数量超过 CPU 核心数，就会发生--线程上下文切换--，导致额外的开销和性能下降。所以线程不能创建得过多
        taskExecutor.setCorePoolSize(CPU_NUM);
        // IO密级 ：2 * N CPU密级：1 + N
        // 最大线程数：线程池中允许的线程数量的最大值。
        // -> 当线程数 = maxPoolSize最大线程数时，还有新任务，就会放进队列中等待执行 ↓↓↓
        taskExecutor.setMaxPoolSize(2 * CPU_NUM);
        // 队列长度：当核心线程数达到最大时，新任务会放在队列中排队等待执行
        // -> 根据业务配置，如果队列长度过大，可能会导致系统内存资源占用过高，最终导致 OOM，需要注意控制
        // -> 如果需要执行的任务装满了队列，就会走拒绝策略 ↓↓↓
        taskExecutor.setQueueCapacity(500);
        // 当前线程池的等待时间：指等待所有任务执行完毕后线程池的最长时间。300秒 = 5分钟
        // -> 当所有任务执行完毕后，线程池会等待一段时间（即等待时间），来确保所有任务都已经完成。
        // -> 如果在等待时间内所有任务仍未完成，则线程池会强制停止，以确保任务不会无限制地执行下去。
        taskExecutor.setAwaitTerminationSeconds(300);
        // 空闲线程存活时间(默认60s)：设置当前线程池中空闲线程的存活时间，即线程池中的线程如果有一段时间没有任务可执行，则会被回收掉。
        // -> 当线程池中的线程数大于 corePoolSize 时，多余的空闲线程将在销毁之前等待新任务的最长时间。
        // -> 如果一个线程在空闲时间超过了 keepAliveSeconds，且当前线程池中线程数量大于 corePoolSize，则该线程将会被回收；
        // -> 核心线程会一直存活，除非线程池被关闭 或 设置下面的参数
        // -> 如果 AllowCoreThreadTimeout设置为true，核心线程也会被回收，直到线程池中的线程数降为 0。
        // 但如果线程池中有任务在执行，那么空闲线程就会一直保持存活状态，直到任务执行完毕。
        // -> 该方法的使用可以将线程池的空闲线程回收，以减少资源占用，同时也能保证线程池中始终有可用的线程来执行任务，提高线程池的效率。
        taskExecutor.setKeepAliveSeconds(60);
        // 当前线程池是否在关闭时等待所有任务执行完成
        // -> 可以确保所有任务都执行完毕后才关闭线程池，避免任务被丢弃，同时也确保线程池可以正常结束，释放资源。
        // -> 为 true 时，线程池在关闭时会等待所有任务都执行完成后再关闭
        // -> 为 false 时，线程池会直接关闭，未执行完成的任务将被丢弃。
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        // 是否禁止线程池自动终止空闲的核心线程。
        // 为 true 时，空闲的核心线程会在 keepAliveTime 时间后被回收，并且在后续任务到来时需要重新创建线程来执行任务。
        // 为 false 时，线程池中的核心线程不会被回收，即使它们处于空闲状态一段时间。
        // -> 在线程池创建时，就会预先创建核心线程数的线程，这些线程将一直存在，除非线程池被关闭或重新配置。
        taskExecutor.setAllowCoreThreadTimeOut(true);
        // 设置拒绝处理的策略(当线程池无法处理新的任务时,该执行什么策略)
        // new ThreadPoolExecutor.CallerRunsPolicy() 该策略为选择调用者线程进行处理
        // new ThreadPoolExecutor.AbortPolicy() 该策略为丢弃任务并抛出RejectedExecutionException异常(不设置时默认此策略)
        // new ThreadPoolExecutor.DiscardPolicy() 该策略为丢弃任务,但是不抛异常
        // new ThreadPoolExecutor.DiscardOldestPolicy() 该策略为丢弃队列最前面的任务,然后重新尝试执行任务
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        // 线程池名称前缀
        taskExecutor.setThreadNamePrefix(threadName + "-");
        // 线程池初始化
        taskExecutor.initialize();
        log.info("初始化线程池完成:{},核心线程为:{}.", threadName, taskExecutor.getCorePoolSize());
        return taskExecutor;
    }

    /**
     * 初始化线程池
     *
     * @return Result
     */
    @Bean("LogThreadPool")
    public Executor logThreadPool() {
        String threadName = "Log-thread-pool";
        log.info("开始初始化线程池:{}", threadName);
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(9000);
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix(threadName + "-");
        executor.initialize();
        log.info("初始化线程池完成:{},核心线程为:{}.", threadName, executor.getCorePoolSize());
        return executor;
    }
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/6/14 Leo creat
 */
