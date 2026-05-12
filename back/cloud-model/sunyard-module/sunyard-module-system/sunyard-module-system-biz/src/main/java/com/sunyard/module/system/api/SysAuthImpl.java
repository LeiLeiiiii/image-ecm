package com.sunyard.module.system.api;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.mybatis.util.PageCopyListUtils;
import com.sunyard.module.system.api.dto.SysApiDTO;
import com.sunyard.module.system.api.dto.SysApiSystemDTO;
import com.sunyard.module.system.mapper.SysApiMapper;
import com.sunyard.module.system.mapper.SysApiSystemMapper;
import com.sunyard.module.system.po.SysApi;
import com.sunyard.module.system.po.SysApiSystem;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author P-JWei
 * @date 2023/7/20 14:10:12
 * @title
 * @description
 */
@RestController
public class SysAuthImpl implements SysAuthApi {

    @Resource
    private SysApiSystemMapper sysApiSystemMapper;
    @Resource
    private SysApiMapper sysApiMapper;

    @Override
    public Result<List<SysApiSystemDTO>> searchAll(SysApiSystemDTO apiSystemDTO) {
        LambdaQueryWrapper<SysApiSystem> apiSysLambdaQueryWrapper = new LambdaQueryWrapper<SysApiSystem>()
                .like(apiSystemDTO != null && StringUtils.hasText(apiSystemDTO.getAppId()),
                        SysApiSystem::getAppId, apiSystemDTO.getAppId())
                .like(apiSystemDTO != null && StringUtils.hasText(apiSystemDTO.getSystemName()),
                        SysApiSystem::getSystemName, apiSystemDTO.getSystemName());
        List<SysApiSystem> apiSystemList = sysApiSystemMapper.selectList(apiSysLambdaQueryWrapper);

        return Result.success(BeanUtil.copyToList(apiSystemList, SysApiSystemDTO.class));
    }

    @Override
    public Result<List<SysApiDTO>> getApiAll(SysApiDTO sysApiDTO) {
        List<SysApi> sysApis = sysApiMapper.selectList(new LambdaQueryWrapper<SysApi>()
                .like(StringUtils.hasText(sysApiDTO.getApiCode()), SysApi::getApiCode, sysApiDTO.getApiCode())
                .like(StringUtils.hasText(sysApiDTO.getApiName()), SysApi::getApiName, sysApiDTO.getApiName())
                .eq(sysApiDTO.getSystemType() != null, SysApi::getSystemType, sysApiDTO.getSystemType())
                .orderByAsc(SysApi::getCreateTime));
        List<SysApiDTO> sysApiDTOS = PageCopyListUtils.copyListProperties(sysApis, SysApiDTO.class);
        return Result.success(sysApiDTOS);
    }

    @Override
    public Result<SysApiSystemDTO> getApiByAppId(String appId) {
        SysApiSystem sysApiSystem = sysApiSystemMapper
                .selectOne(new LambdaQueryWrapper<SysApiSystem>()
                        .eq(SysApiSystem::getAppId, appId)
                        .eq(SysApiSystem::getStatus, 0));
        return Result.success(BeanUtil.copyProperties(sysApiSystem, SysApiSystemDTO.class));
    }

}
