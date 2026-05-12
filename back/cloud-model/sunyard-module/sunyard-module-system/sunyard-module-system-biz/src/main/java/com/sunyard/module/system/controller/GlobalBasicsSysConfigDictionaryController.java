package com.sunyard.module.system.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.constant.LogsPrefixConstants;
import com.sunyard.module.system.service.SysDictionaryService;

/**
 * 全局/字典表配置
 *
 * @author wubingyang
 * @date 2021/7/21 9:00
 */
@RestController
@RequestMapping("global")
public class GlobalBasicsSysConfigDictionaryController {
    private static final String BASELOG = LogsPrefixConstants.GLOBAL_DICTION + "->";
    @Resource
    private SysDictionaryService sysDictionaryService;

    /**
     * 根据key查询字典表
     *
     * @param key        key
     * @param systemCode 系统code
     * @return result
     */
    @OperationLog(BASELOG + "根据key查询字典表")
    @PostMapping("selectValueByKey")
    public Result selectValueByKey(String key, Integer systemCode) {
        return Result.success(sysDictionaryService.getDictionaryAll(key, systemCode));
    }
}
