package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysParamDTO;
import com.sunyard.module.system.po.SysParam;
import com.sunyard.module.system.service.SysParamService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author P-JWei
 * @date 2023/5/15 15:57 @title：
 * @description:
 */
@RestController
public class ParamApiImpl implements ParamApi {

    @Resource
    private SysParamService service;

    @Override
    public Result<SysParamDTO> searchValueByKey(String key) {
        SysParam po = service.searchValueByKey(key);
        SysParamDTO dto = new SysParamDTO();
        BeanUtils.copyProperties(po, dto);
        return Result.success(dto);
    }

    @Override
    public Result<Boolean> updateValueByKey(String key, String value) {
        service.updateValueByKey(key, value);
        return Result.success();
    }

    @Override
    public Result<Boolean> updateValueAndStatusByKey(String key, String value, Integer status) {
        service.updateValueAndStatusByKey(key, value,status);
        return Result.success();
    }
}
