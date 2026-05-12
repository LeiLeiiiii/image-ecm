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
import com.sunyard.module.system.api.dto.SysApiDTO;
import com.sunyard.module.system.api.dto.SysApiSystemDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 接入授权系统API
 *
 * @author zzwang
 * @date 2024/5/6 10:20
 */
@FeignClient(name = ApiConstants.NAME)
public interface SysAuthApi {

    String PREFIX = ApiConstants.PREFIX + "/sysAuth/";

    /**
     * 查询所有接入系统列表
     *
     * @param sysApiSystemDTO 接入系统DTO
     * @return
     */
    @PostMapping(PREFIX + "searchAll")
    Result<List<SysApiSystemDTO>> searchAll(@RequestBody SysApiSystemDTO sysApiSystemDTO);

    /**
     * 获取接对外口
     * @param sysApiDTO
     * @return
     */
    @PostMapping(PREFIX + "getApiAll")
    Result<List<SysApiDTO>> getApiAll(@RequestBody SysApiDTO sysApiDTO);

    /**
     * 通过appid获取第三方授权信息
     * @param appId
     * @return
     */
    @PostMapping(PREFIX + "getApiByAppId")
    Result<SysApiSystemDTO> getApiByAppId(@RequestParam("appId") String appId);

}

