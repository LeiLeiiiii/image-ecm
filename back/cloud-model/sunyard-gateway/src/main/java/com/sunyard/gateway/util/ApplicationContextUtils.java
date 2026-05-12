package com.sunyard.gateway.util;

/*
 * Project: Sunyard
 *
 * File Created at 2023/5/12
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/12 10:07
 */
public class ApplicationContextUtils {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (ApplicationContextUtils.applicationContext == null) {
            ApplicationContextUtils.applicationContext = applicationContext;
        }
    }

    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/12 Leo creat
 */