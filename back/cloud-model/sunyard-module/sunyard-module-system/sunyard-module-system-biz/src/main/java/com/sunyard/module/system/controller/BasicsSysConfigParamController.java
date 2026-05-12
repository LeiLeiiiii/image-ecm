package com.sunyard.module.system.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.po.SysParam;
import com.sunyard.module.system.service.SysParamService;

/**
 * 通用管理/系统初始化/系统配置
 *
 * @author wubingyang
 * @date 2021/7/21 9:00
 */
@RestController
@RequestMapping("basics/sysConfig/param")
public class BasicsSysConfigParamController {
    private static final String BASELOG = LogsPrefixConstants.MENU_SYSTEM + "-系统参数->";
    @Resource
    private SysParamService service;

    /**
     * 新增
     *
     * @param sysParam 配置obj
     * @return Result
     */
    @OperationLog(BASELOG + "新增")
    @PostMapping("addParam")
    public Result addParam(@RequestBody SysParam sysParam) {
        service.addParam(sysParam);
        return Result.success(true);
    }

    /**
     * 修改
     *
     * @param sysParam 配置obj
     * @return Result
     */
    @OperationLog(BASELOG + "修改")
    @PostMapping("updateParam")
    public Result updateParam(@RequestBody SysParam sysParam) {
        service.updateParam(sysParam);
        return Result.success(true);
    }

    /**
     * 删除
     *
     * @param id 配置id
     * @return Result
     */
    @OperationLog(BASELOG + "删除")
    @PostMapping("delParam")
    public Result delParam(Long id) {
        service.delParam(id);
        return Result.success(true);
    }

    /**
     * 详情
     *
     * @param id 配置id
     * @return Result
     */
    @OperationLog(BASELOG + "详情")
    @PostMapping("getInfoParam")
    public Result getInfoParam(Long id) {
        return Result.success(service.getInfoParam(id));
    }

    /**
     * 根据key获取
     *
     * @param sysParam 配置obj
     * @param pageForm 分页参数
     * @return Result
     */
    @OperationLog(BASELOG + "根据key获取")
    @PostMapping("getListParam")
    public Result getListParam(SysParam sysParam, PageForm pageForm) {
        return Result.success(service.getListParam(sysParam, pageForm));
    }
}
