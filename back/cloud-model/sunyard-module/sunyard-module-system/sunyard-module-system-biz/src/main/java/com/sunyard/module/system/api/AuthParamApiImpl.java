package com.sunyard.module.system.api;

import com.sunyard.framework.common.result.Result;
import com.sunyard.module.system.api.dto.SysConfigLoginDTO;
import com.sunyard.module.system.service.SysParamService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 通用管理-参数配置
 *
 * @author wubingyang
 * @date 2021/7/21 9:00
 */
@RestController
public class AuthParamApiImpl implements AuthParamApi {

    @Resource
    private SysParamService service;

    @Override
    public Result<SysConfigLoginDTO> select() {
        return Result.success(service.select());
    }

}
