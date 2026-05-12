package com.sunyard.module.auth.api;
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

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.auth.constant.ApiConstants;
import com.sunyard.module.auth.constant.TokenConstants;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/6 10:20
 */
@FeignClient(name = ApiConstants.NAME)
public interface GatewayApi {
    String PREFIX = ApiConstants.PREFIX + "/gateway/";

    /**
     * 接口cookie校验
     *
     * @param token cookie的token参数
     * @param url 请求原始的url
     * @return Result
     */
    @PostMapping(value = PREFIX + "isPermitted")
    Result<Map<String,String>> isPermitted(@CookieValue(TokenConstants.SUNYARD_TOKEN) String token, @CookieValue("url") String url);
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/6 Leo creat
 */