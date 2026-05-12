/*
 * Project: smartzone-common
 *
 * File Created at 2018年9月11日
 *
 * Copyright 2016 CMCC Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of ZYHY Company. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */
package com.sunyard.framework.mybatis.util;

import cn.hutool.core.util.IdUtil;

/**
 * @author zhouleibin
 */
public class SnowflakeUtils {
    private static SnowflakeUtils snowflakeUtils = new SnowflakeUtils();

    /**
     * 单例
     */
    public static SnowflakeUtils build() {
        return snowflakeUtils;
    }

    /**
     * 获取id
     */
    public long nextId() {
        return IdUtil.getSnowflake().nextId();
    }

}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2018年9月11日 zhouleibin-bwf
 * creat
 */
