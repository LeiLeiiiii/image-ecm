package com.sunyard.module.system.controller.api;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.ApiLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.SysConfigWaterMarkService;

/**
 * @author liuwen
 * @Description: 屏幕水印对外接口
 * @Date: 2023/12/6
 */
@RestController
@RequestMapping("api/basics/sysConfig/waterMark")
public class SysConfigWaterMarkController {
    private static final String BASELOG = LogsPrefixConstants.API_WATERMARK + "->";

    @Resource
    private SysConfigWaterMarkService sysConfigWaterMarkService;

    /**
     * 查询配置信息
     * @return result
     */
    @ApiLog(BASELOG + "查询")
    @PostMapping("selectWaterMark")
    public Result selectWaterMark() {
        return Result.success(sysConfigWaterMarkService.selectWaterMark());
    }
}
