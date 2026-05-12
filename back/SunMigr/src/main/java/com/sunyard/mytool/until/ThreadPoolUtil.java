package com.sunyard.mytool.until;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolUtil {
    private static Logger logger = LoggerFactory.getLogger(ThreadPoolUtil.class.getName());

    public static ThreadPoolTaskExecutor getThreadPoolTaskExecutor(int coreSize, int maxSize, int queueSize, String threadName,
                                                                   int KeepAliveSeconds) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //核心线程数量
        threadPoolTaskExecutor.setCorePoolSize(coreSize);
        //最大线程数量
        threadPoolTaskExecutor.setMaxPoolSize(maxSize);
        //队列中最大任务数
        threadPoolTaskExecutor.setQueueCapacity(queueSize);
        //线程名称前缀
        threadPoolTaskExecutor.setThreadNamePrefix(threadName);
        //当达到最大线程数时如何处理新任务
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        //线程空闲后最大存活时间
        threadPoolTaskExecutor.setKeepAliveSeconds(KeepAliveSeconds);
        // 自定义处理增强方法
        threadPoolTaskExecutor.setTaskDecorator(task -> {
            // 获取主线程的 MDC 上下文（map 形式）
            Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    // 如果存在上下文，则将主线程的 traceId 等信息传递给线程池的子线程
                    if (copyOfContextMap != null && !copyOfContextMap.isEmpty()) {
                        MDC.setContextMap(copyOfContextMap);
                    }
                    task.run();
                } finally {
                    MDC.clear();
                }
            };
        });
        //初始化线程池
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
