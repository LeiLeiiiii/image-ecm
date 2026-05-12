package com.sunyard.ecm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author scm
 * @since 2023/8/1 14:17
 * @Desc 业务日志自定义注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogManageAnnotation {

    /**
     * 操作内容
     */
    String value() default "";

}
