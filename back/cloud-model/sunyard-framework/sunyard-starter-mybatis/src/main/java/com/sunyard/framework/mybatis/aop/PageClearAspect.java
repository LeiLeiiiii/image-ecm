package com.sunyard.framework.mybatis.aop;
/*
 * Project: com.reader.aop
 *
 * File Created at 2020/11/23
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * @Desc 预防代码不规范导致的分页漏洞
 * @author zhouleibin
 * @date 2020/11/23 上午11:05
 * @version
 */
@Slf4j
@Aspect
public class PageClearAspect {

    /**
     * 预防代码不规范导致的分页漏洞
     * @param pjp 切入点
     * @return obj
     * @throws Throwable 异常
     */
    @Around("execution(* com.sunyard.cloud.service..*.*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        PageHelper.clearPage();
        return pjp.proceed();
    }

}
/**
 * Revision history -------------------------------------------------------------------------
 *
 * Date Author Note ------------------------------------------------------------------------- 2020/11/23 zhouleibin
 * creat
 */
