package com.sunyard.module.system.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysApiSystemDTO;
import com.sunyard.module.system.mapper.SysApiAuthMapper;
import com.sunyard.module.system.mapper.SysApiMapper;
import com.sunyard.module.system.mapper.SysApiSystemMapper;
import com.sunyard.module.system.po.SysApi;
import com.sunyard.module.system.po.SysApiAuth;
import com.sunyard.module.system.po.SysApiSystem;

/**
 * @author P-JWei
 * @date 2023/7/20 14:10:12
 * @title
 * @description
 */
@RestController
public class ApiAuthApiImpl implements ApiAuthApi {

    @Resource
    private SysApiAuthMapper sysApiAuthMapper;

    @Resource
    private SysApiMapper sysApiMapper;

    @Resource
    private SysApiSystemMapper sysApiSystemMapper;

    @Override
    public Result<List<String>> getApiCodeByAppId(String appId) {
        SysApiSystem sysApiSystems = sysApiSystemMapper
                .selectOne(new LambdaQueryWrapper<SysApiSystem>()
                        .eq(SysApiSystem::getAppId, appId)
                        .eq(SysApiSystem::getStatus, 0));
        if (ObjectUtils.isEmpty(sysApiSystems)) {
            return Result.success(new ArrayList<>());
        }
        List<SysApiAuth> sysApiAuths = sysApiAuthMapper
                .selectList(new LambdaUpdateWrapper<SysApiAuth>()
                        .eq(SysApiAuth::getAppId, sysApiSystems.getId())
                );
        List<Long> apiIds = sysApiAuths.stream()
                .map(SysApiAuth::getApiId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(apiIds)) {
            return Result.success(new ArrayList<>());
        }
        List<SysApi> apiList = sysApiMapper
                .selectList(new LambdaQueryWrapper<SysApi>()
                        .in(SysApi::getId, apiIds));
        List<String> apiCodeList = apiList.stream()
                .map(SysApi::getApiCode)
                .collect(Collectors.toList());
        return Result.success(apiCodeList);
    }

    @Override
    public Result<SysApiSystemDTO> getAppObjByAppId(String appId) {
        SysApiSystem system = sysApiSystemMapper
                .selectOne(new LambdaQueryWrapper<SysApiSystem>()
                        .eq(SysApiSystem::getAppId, appId));
        SysApiSystemDTO result = new SysApiSystemDTO();
        BeanUtils.copyProperties(system, result);
        return Result.success(result);
    }

    @Override
    public Result<String> getPublicKeyByAppId(String appId) {
        SysApiSystem sysApiSystems = sysApiSystemMapper
                .selectOne(new LambdaQueryWrapper<SysApiSystem>()
                        .eq(SysApiSystem::getAppId, appId));
        if (ObjectUtils.isEmpty(sysApiSystems)) {
            return Result.success("");
        }
        return Result.success(sysApiSystems.getPublicKey());
    }

    @Override
    public Result<String> getRefererByAppId(String appId) {
        SysApiSystem sysApiSystems = sysApiSystemMapper
                .selectOne(new LambdaQueryWrapper<SysApiSystem>()
                        .eq(SysApiSystem::getAppId, appId));
        if (ObjectUtils.isEmpty(sysApiSystems)) {
            return Result.success("");
        }
        return Result.success(sysApiSystems.getSystemReferer());
    }

    @Override
    public Result<Boolean> getIsAuthByAppId(String appId) {
        SysApiSystem sysApiSystems = sysApiSystemMapper
                .selectOne(new LambdaQueryWrapper<SysApiSystem>()
                        .eq(SysApiSystem::getAppId, appId));
        if (!ObjectUtils.isEmpty(sysApiSystems) && 0 == sysApiSystems.getStatus()) {
            return Result.success(true);
        }
        return Result.success(false);
    }

    @Override
    public Result<Map<String, String>> getAllApiUrl(Integer filterType) {
        LambdaQueryWrapper<SysApi> wrapper = new LambdaQueryWrapper<>();
        switch (filterType) {
            case 1:
                wrapper.eq(SysApi::getIsTimestamp, 1);
                break;
            case 2:
                wrapper.eq(SysApi::getIsReferer, 1);
                break;
            case 3:
                wrapper.eq(SysApi::getIsSign, 1);
                break;
            default:
        }
        List<SysApi> sysApis = sysApiMapper.selectList(wrapper);
        Map<String, String> map = new HashMap<>(6);
        if (!CollectionUtils.isEmpty(sysApis)) {
            map = sysApis.stream().collect(Collectors.toMap(SysApi::getApiCode, SysApi::getApiUrl));
        }
        return Result.success(map);
    }

    @Override
    public Result<String> getSecretKeyByPublicKey(String publicKey) {
        SysApiSystem sysApiSystem = sysApiSystemMapper
                .selectOne(new LambdaQueryWrapper<SysApiSystem>()
                        .eq(SysApiSystem::getPublicKey, publicKey));
        if (ObjectUtils.isEmpty(sysApiSystem)) {
            return Result.success("");
        }
        return Result.success(sysApiSystem.getPrivateKey());
    }


}
