package com.sunyard.ecm.aspect;

import com.sunyard.ecm.controller.BaseController;
import com.sunyard.framework.log.annotation.ApiLog;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;


/**
 * @author lw
 * @since 2024/3/13
 * @Desc 方式时间统计切面
 */
//@Component
@Aspect
@Slf4j
public class TimeCalculateAspect extends BaseController {

    /**
     * 统计方法执行时间
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@within(com.sunyard.ecm.annotation.TimeCalculateAnnotation)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        // 获取方法上的@ApiOperation注解或者@ApiLog注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String operationValue = "";
        if (method.isAnnotationPresent(ApiOperation.class)) {
            ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);
            if (apiOperation != null) {
                operationValue = apiOperation.value();
            }
        } else if (method.isAnnotationPresent(ApiLog.class)) {
            ApiLog apiLog = AnnotationUtils.findAnnotation(method, ApiLog.class);
            if (apiLog != null) {
                operationValue = apiLog.value();
            }
        }
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        log.info(operationValue + "方法" + "【" + joinPoint.getSignature() + "】" + " 耗时:" + executionTime + "ms" );
        return result;
    }

}
