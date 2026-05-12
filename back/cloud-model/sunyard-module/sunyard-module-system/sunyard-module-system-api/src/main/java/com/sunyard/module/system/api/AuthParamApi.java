package com.sunyard.module.system.api;
/*
 * Project: Sunyard
 *
 * File Created at 2023/5/4
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysConfigLoginDTO;
import com.sunyard.module.system.constant.ApiConstants;

/**
 * @author Leo
 * @Desc
 * @date 2023/5/4 17:53
 */
@FeignClient(name = ApiConstants.NAME)
public interface AuthParamApi {

    String PREFIX = ApiConstants.PREFIX + "/authParam/";

    /**
     * 查询登录配置
     * @return Result 配置obj
     */
    @PostMapping(PREFIX + "select")
    Result<SysConfigLoginDTO> select();
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/4 Leo creat
 */
