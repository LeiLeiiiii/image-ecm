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
import com.sunyard.module.system.api.dto.SysUserAdminDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/6 10:20
 */
@FeignClient(name = ApiConstants.NAME)
public interface AuthOrgApi {

    String PREFIX = ApiConstants.PREFIX + "/authOrg/";

    /**
     * 查询管理员信息
     * @param userId 管理员用户id
     * @return Result 管理员信息
     */
    @PostMapping(PREFIX + "selectSuperOrg")
    Result<SysUserAdminDTO> selectSuperOrg(@RequestParam("userId") Long userId);

    /**
     * 查询用户信息
     * @param userId 用户id
     * @return Result 用户信息
     */
    @PostMapping(PREFIX + "selectOrg")
    Result<SysUserDTO> selectOrg(@RequestParam("userId") Long userId);
}