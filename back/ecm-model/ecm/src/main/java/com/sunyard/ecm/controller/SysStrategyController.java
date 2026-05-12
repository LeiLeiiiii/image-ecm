package com.sunyard.ecm.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.service.SysStrategyService;
import com.sunyard.ecm.vo.SysStrategyVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author scm
 * @since 2023/7/27 13:58
 * @Desc 系统管理-策略管理
 */
@Api(tags = "系统管理-策略管理")
@RestController
@RequestMapping("sys/strategy")
public class SysStrategyController extends BaseController {

    @Resource
    private SysStrategyService sysStrategyService;

    /**
     * 业务类型树查询接口
     */
    @ApiOperation("业务类型树查询接口")
    @OperationLog(LogsConstants.SYSTEM + "业务类型树查询接口")
    @PostMapping("tree")
    public Result getAppTypeTree(@RequestBody List<String> appTypeIds) {
        return Result.success(sysStrategyService.getAppTypeTree(appTypeIds, getToken()));
    }

    /**
     * 策略管理配置查询接口
     */
    @ApiOperation("策略管理配置查询接口")
    @OperationLog(LogsConstants.SYSTEM + "策略管理配置查询接口")
    @PostMapping("query")
    public Result queryConfig() {
        return Result.success(sysStrategyService.queryConfig());
    }

    /**
     * 策略管理配置更新接口
     */
    @ApiOperation("策略管理配置更新接口")
    @OperationLog(LogsConstants.SYSTEM + "策略管理配置更新接口")
    @PostMapping("update")
    public Result updateConfig(@RequestBody SysStrategyVO sysStrategyVO) {
        return Result.success(sysStrategyService.updateConfig(sysStrategyVO, getToken()));
    }

}
