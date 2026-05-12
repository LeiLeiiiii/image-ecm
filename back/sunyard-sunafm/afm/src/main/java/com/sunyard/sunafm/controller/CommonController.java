package com.sunyard.sunafm.controller;

import com.sunyard.framework.common.result.Result;
import com.sunyard.sunafm.service.CommonService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author P-JWei
 * @date 2024/3/7 16:00:01
 * @title
 * @description  通用方法
 */
@RestController
@RequestMapping("common")
public class CommonController extends BaseController{
    @Resource
    private CommonService commonService;
    /**
     * 获取阈值
     */
    @PostMapping("getSimpleDefult")
    public Result<Double> getSimpleDefult(){
        return Result.success(commonService.getSimpleDefult());
    }
}
