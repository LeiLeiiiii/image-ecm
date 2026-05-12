package com.sunyard.framework.redis.annotation;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhouleibin
 * @date 2021/12/1 16:03
 * @Desc
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLimit {
    // 标识 指定time时间段内的访问次数限制
    int limit() default 5;

    // 标识 时间段秒
    int time() default 5;

    // 提示信息
    String message() default "请稍等下";
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/12/1 zhouleibin creat
 */
