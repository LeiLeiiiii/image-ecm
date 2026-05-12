package com.sunyard.framework.log.aop;
/*
 * Project: com.sunyard.am.aop
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sunyard.framework.log.constant.LogConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 */
@Slf4j
@Aspect
@Order(3)
@Component
public class ConsoleFeginAspect extends BaseAspect {

    private static final ThreadLocal<Long> BEGIN_TIME_THREAD_LOCAL =
        new NamedThreadLocal<>("Fegin ThreadLocal beginTime");

    /**
     * 针对fegin的日志
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)"
        + "&& !@annotation(org.springframework.web.bind.annotation.PostMapping)"
        + "&& !@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void pointCut() {}

    /**
     * 输出插入、返参日志
     * @param point 切入点
     * @return obj
     * @throws Throwable 异常
     */
    @Around(value = "pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        BEGIN_TIME_THREAD_LOCAL.set(System.nanoTime());
        super.inputConsoleLogs(point, LogConstants.STOREY_FEGIN, null);
        Object result = point.proceed();

        long elapsedTimeInMillis = (System.nanoTime() - BEGIN_TIME_THREAD_LOCAL.get()) / 1000000;

        super.outputConsoleLogs(point, LogConstants.STOREY_FEGIN, null, elapsedTimeInMillis);
        return result;
    }

}
