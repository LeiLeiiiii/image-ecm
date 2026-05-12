package com.sunyard.module.system.api;
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

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysApiSystemDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/6 10:20
 */
@FeignClient(name = ApiConstants.NAME)
public interface ApiAuthApi {

    String PREFIX = ApiConstants.PREFIX + "/apiAuth/";

    /**
     * 获取指定app的具有权限的apiCode
     * @param appId appId
     * @return Result
     */
    @PostMapping(PREFIX + "getApiCodeByAppId")
    Result<List<String>> getApiCodeByAppId(@RequestParam("appId")String appId);

    /**
     * 根据appId获取appSecret
     * @param appId appId
     * @return Result
     */
    @PostMapping(PREFIX + "getAppSecretByAppId")
    Result<SysApiSystemDTO> getAppObjByAppId(@RequestParam("appId")String appId);

    /**
     * 根据appId获取公钥
     * @param appId appId
     * @return Result
     */
    @PostMapping(PREFIX + "getPublicKeyByAppId")
    Result<String> getPublicKeyByAppId(@RequestParam("appId")String appId);

    /**
     * 根据appId获取Referer集
     * @param appId appId
     * @return Result
     */
    @PostMapping("getRefererByAppId")
    Result<String> getRefererByAppId(@RequestParam("appId") String appId);

    /**
     * 根据appId判断是否授权
     * @param appId appId
     * @return Result
     */
    @PostMapping("getIsAuthByAppId")
    Result<Boolean> getIsAuthByAppId(@RequestParam("appId") String appId);

    /**
     * 获取所有的对接api的url
     * @param filterType 0获取全部 1时间戳Filter 2RefererFilter 3验签Filter
     * @return Result
     */
    @PostMapping("getAllApiUrl")
    Result<Map<String,String>> getAllApiUrl(@RequestParam("filterType") Integer filterType);

    @PostMapping("getSecretKeyByPublicKey")
    Result<String> getSecretKeyByPublicKey(@RequestParam("publicKey")String publicKey);
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/6 Leo creat
 */
