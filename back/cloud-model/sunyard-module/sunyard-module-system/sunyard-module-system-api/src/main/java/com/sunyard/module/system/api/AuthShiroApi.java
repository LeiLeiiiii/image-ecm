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
import com.sunyard.module.system.api.dto.SysMenuDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/6 10:20
 */
@FeignClient(name = ApiConstants.NAME)
public interface AuthShiroApi {

    String PREFIX = ApiConstants.PREFIX + "/authShiro/";

    /**
     * 查询系统菜单
     *
     * @return Result
     */
    @PostMapping(PREFIX + "searchShiroPaths")
    Result<List<SysMenuDTO>> searchShiroPaths();


    /**
     * 根据Perms拿到Path
     * @param perms 菜单标识
     * @return Result
     */
    @PostMapping("getMenuPathByPerms")
    Result<String> getMenuPathByPerms(@RequestParam("perms") String perms);

}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/6 Leo creat
 */