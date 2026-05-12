package com.sunyard.module.system.constant;
/*
 * Project: Sunyard
 *
 * File Created at 2023/5/6
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.sunyard.framework.rpc.constant.RpcConstants;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/6 10:26
 */
public class ApiConstants {
    /**
     * 服务名
     * <p>
     * 注意，需要保证和 spring.application.name 保持一致
     */
    public static final String NAME = "system-service";

    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/system";

    public static final String VERSION = "1.0.0";
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/6 Leo creat
 */
