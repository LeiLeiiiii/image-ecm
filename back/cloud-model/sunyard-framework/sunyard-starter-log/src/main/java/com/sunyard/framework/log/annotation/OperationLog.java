package com.sunyard.framework.log.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author P-JWei
 * @description: 业务controller注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 日志标题
     * @return Result
     */
    String value() default "";

    /**
     * 可指定记录参数、不指定记录全部
     * @return Result
     */
    String param() default "";

}
