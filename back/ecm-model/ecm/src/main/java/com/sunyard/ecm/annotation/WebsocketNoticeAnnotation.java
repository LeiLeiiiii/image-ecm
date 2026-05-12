package com.sunyard.ecm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author lw
 * @since 2023-5-30
 * @Desc websocket通知自定义注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebsocketNoticeAnnotation {

    /**
     * 业务批次号
     */
    String busiId() default "";

    /**
     * 消息类型
     */
    String msgType() default "";


    /**
     * 消息
     */
    String msg() default "";
}
