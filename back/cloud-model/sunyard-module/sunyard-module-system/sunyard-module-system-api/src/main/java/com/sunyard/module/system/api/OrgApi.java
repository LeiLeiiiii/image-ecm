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
import com.sunyard.module.system.api.dto.SysInstDTO;
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
@FeignClient(value = ApiConstants.NAME)
public interface OrgApi {

    String PREFIX = ApiConstants.PREFIX + "/org/";

    /**
     * 查询机构所有层级数据
     *
     * @param instId 机构id
     * @return Result
     */
    @PostMapping(PREFIX + "searchInst")
    Result<List<SysInstDTO>> searchInst(@RequestParam("instId") Long instId);

    /**
     * 查询所有机构
     *
     * @return Result
     */
    @PostMapping(PREFIX + "getInstAll")
    Result<List<SysInstDTO>> getInstAll();

}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/5/6 Leo creat
 */
