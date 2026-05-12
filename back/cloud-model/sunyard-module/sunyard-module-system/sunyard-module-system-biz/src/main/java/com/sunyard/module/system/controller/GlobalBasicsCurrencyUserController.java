package com.sunyard.module.system.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.OrgUserService;

/**
 * @author P-JWei
 * @date 2024/1/16 10:26:00
 * @title
 * @description
 */
@RestController
@RequestMapping("global/user")
public class GlobalBasicsCurrencyUserController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.GLOBAL_USER + "->";
    @Resource
    private OrgUserService orgUserService;

    /**
     * 查询所有用户
     *
     * @return Result
     */
    @OperationLog(BASELOG + "查询所有用户")
    @PostMapping("searchAll")
    public Result searchAll() {
        return Result.success(orgUserService.searchAll());
    }
}
