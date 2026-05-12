package com.sunyard.sunafm.controller;

import com.sunyard.framework.common.result.Result;
import com.sunyard.sunafm.service.CommonService;
import com.sunyard.sunafm.service.SysThresholdService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author P-JWei
 * @date 2024/3/7 16:00:01
 * @title
 * @description  系统管理/阈值配置
 */
@RestController
@RequestMapping("sys/threshold")
public class SysThresholdController extends BaseController{
    @Resource
    private CommonService commonService;
    @Resource
    private SysThresholdService sysThresholdService;
    /**
     * 获取阈值
     */
    @PostMapping("getSimpleDefult")
    public Result<Double> getSimpleDefult(){
        return Result.success(commonService.getSimpleDefult());
    }
    /**
     * 配置阈值
     */
    @PostMapping("updateSimpleDefult")
    public Result updateSimpleDefult(String value){
        sysThresholdService.updateSimpleDefult(value);
        return Result.success();
    }
}
