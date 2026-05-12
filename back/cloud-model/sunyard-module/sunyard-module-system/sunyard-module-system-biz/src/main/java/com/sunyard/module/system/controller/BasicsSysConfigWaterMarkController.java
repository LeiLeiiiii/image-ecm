package com.sunyard.module.system.controller;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.po.SysConfigWatermark;
import com.sunyard.module.system.po.SysParam;
import com.sunyard.module.system.service.SysConfigWaterMarkService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 通用管理/系统初始化/水印
 *
 * @author wubingyang
 * @date 2021/7/21 9:00
 */
@RestController
@RequestMapping("basics/sysConfig/waterMark")
public class BasicsSysConfigWaterMarkController {
    private static final String BASELOG = LogsPrefixConstants.MENU_SYSTEM + "-水印配置->";
    @Resource
    private SysConfigWaterMarkService sysConfigWaterMarkService;

    /**
     * 查询配置信息
     *
     * @return Result
     */
    @OperationLog(BASELOG + "查询配置信息")
    @PostMapping("select")
    public Result select() {
        return Result.success(sysConfigWaterMarkService.select());
    }

    /**
     * 保存
     *
     * @param config 水印pbj
     * @return Result
     */
    @OperationLog(BASELOG + "保存")
    @PostMapping("saveConfig")
    public Result saveConfig(@RequestBody SysConfigWatermark config) {
        sysConfigWaterMarkService.saveConfig(config);
        return Result.success(true);
    }

    /**
     * 查询配置信息-new
     *
     * @return Result
     */
    @OperationLog(BASELOG + "查询配置信息-new")
    @PostMapping("selectWaterMark")
    public Result selectWaterMark() {
        return Result.success(sysConfigWaterMarkService.selectWaterMark());
    }

    /**
     * 保存
     *
     * @param sysParam 配置obj
     * @return Result
     */
    @OperationLog(BASELOG + "保存")
    @PostMapping("saveWaterMarkConfig")
    public Result saveWaterMarkConfig(@RequestBody SysParam sysParam) {
        sysConfigWaterMarkService.saveWaterMarkConfig(sysParam.getValue(), sysParam.getName());
        return Result.success(true);
    }


}
