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
import com.sunyard.module.system.mapper.SysPostMapper;
import com.sunyard.module.system.mapper.SysPostUserMapper;
import com.sunyard.module.system.po.SysPost;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wml
 * @Desc 岗位模块对内提供的接口实现
 * @date 2025/9/9 14:15
 */
@RestController
public class PostApiImpl implements PostApi{

    @Resource
    private SysPostMapper sysPostMapper;
    @Resource
    private SysPostUserMapper sysPostUserMapper;

    /**
     * 多条件查询岗位信息
     *
     * @param sysPostDTO
     * @return
     */
    @Override
    public Result<List<SysPostDTO>> searchByCondition(SysPostDTO sysPostDTO) {
        return Result.success(sysPostMapper.searchByCondition(sysPostDTO));
    }

    /**
     * 多条件查询岗位下关联的用户信息
     *
     * @param sysPostDTO
     * @return
     */
    @Override
    public Result<List<SysUserDTO>> searchPostUserByCondition(SysPostDTO sysPostDTO) {
        return Result.success(sysPostUserMapper.searchPostUserByCondition(sysPostDTO));
    }

    /**
     * 根据岗位id查询岗位信息
     *
     * @param postIdList
     * @return
     */
    @Override
    public Result<List<SysPostDTO>> searchPostUByPosIdList(@RequestBody List<Long> postIdList) {
        List<SysPost> sysPostList = sysPostMapper.selectBatchIds(postIdList);
        List<SysPostDTO> sysPostDTOList = sysPostList.stream().map(sysPost -> {
            SysPostDTO sysPostDTO = new SysPostDTO();
            BeanUtils.copyProperties(sysPost, sysPostDTO);
            return sysPostDTO;
        }).collect(Collectors.toList());
        return Result.success(sysPostDTOList);
    }
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2025/9/9 wml creat
 */