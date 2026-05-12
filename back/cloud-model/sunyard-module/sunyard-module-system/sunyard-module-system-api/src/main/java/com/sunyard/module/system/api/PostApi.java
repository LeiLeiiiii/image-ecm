package com.sunyard.module.system.api;
/*
 * Project: sunyard
 *
 * File Created at 2025/9/9
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysPostDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author wml
 * @Desc 岗位模块对内提供的接口
 * @date 2025/9/9 11:27
 */
@FeignClient(value = ApiConstants.NAME)
public interface PostApi {

    String PREFIX = ApiConstants.PREFIX + "/post/";

    /**
     * 多条件查询岗位信息
     *
     * @param sysPostDTO
     * @return
     */
    @PostMapping(PREFIX+"searchByCondition")
    Result<List<SysPostDTO>> searchByCondition(@RequestBody SysPostDTO sysPostDTO);

    /**
     * 多条件查询岗位下关联的用户信息
     *
     * @param sysPostDTO
     * @return
     */
    @PostMapping(PREFIX+"searchPostUserByCondition")
    Result<List<SysUserDTO>> searchPostUserByCondition(@RequestBody SysPostDTO  sysPostDTO);

    /**
     * 根据岗位id集合查询岗位信息
     *
     * @param postIdList
     * @return
     */
    @PostMapping(PREFIX + "searchPostUByPosIdList")
    Result<List<SysPostDTO>> searchPostUByPosIdList(@RequestBody List<Long> postIdList);

}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2025/9/9 wml creat
 */