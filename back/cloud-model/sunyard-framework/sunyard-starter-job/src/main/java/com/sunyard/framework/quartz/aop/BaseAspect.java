package com.sunyard.framework.quartz.aop;
/*
 * Project: com.sunyard.am.aspect
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.springframework.util.StringUtils;

import com.sunyard.framework.quartz.constant.LogConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.aspect
 * @Desc
 * @date 2021/6/30 9:27
 */
@Slf4j
public class BaseAspect {
    @Resource
    protected HttpServletRequest request;

    /**
     * 日志输出
     *
     * @param point 切入点
     * @param storeyName storeyName
     * @param param 参数
     */
    protected void inputConsoleLogs(JoinPoint point, String storeyName, String param) {
        String className = point.getSignature().getDeclaringType().getSimpleName();
        String methodName = point.getSignature().getName();
        if (StringUtils.hasText(param)) {
            log.info(String.format("%s [%s %s.%s]%nParameters:[%s]", LogConstants.IN, storeyName,
                    className, methodName, param));
        } else {
            log.info(String.format("%s [%s %s.%s]", LogConstants.IN, storeyName, className,
                    methodName));
        }
    }

    /**
     * 日志输出
     *
     * @param point 切入点
     * @param storeyName storeyName
     * @param param 参数
     */
    protected void outputConsoleLogs(JoinPoint point, String storeyName, String param,
                                     Long elapsedTimeInMillis) {
        String className = point.getSignature().getDeclaringType().getSimpleName();
        String methodName = point.getSignature().getName();
        if (StringUtils.hasText(param)) {
            log.info(String.format("%s [%s %s.%s] [%s ms]%nParameters:[%s]", LogConstants.OUT,
                    storeyName, className, methodName, elapsedTimeInMillis, param));
        } else {
            log.info(String.format("%s [%s %s.%s] [%s ms]", LogConstants.OUT, storeyName,
                    className, methodName, elapsedTimeInMillis));
        }
    }

}
