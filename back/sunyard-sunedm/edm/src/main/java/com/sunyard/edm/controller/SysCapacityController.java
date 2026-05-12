package com.sunyard.edm.controller;


import com.sunyard.edm.constant.DocLogsConstants;
import com.sunyard.edm.service.SysCapacityService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Author PJW 2022/12/12 14:01
 * @Desc 高级配置-容量上限
 */
@RestController
@RequestMapping("sys/capacity")
public class SysCapacityController extends BaseController {

    @Resource
    private SysCapacityService sysCapacityService;

    /**
     * 配置列表
     *
     * @return
     */
    @PostMapping("searchCapacity")
    @OperationLog(DocLogsConstants.CAPACITY + DocLogsConstants.COMMON_GETLIST)
    public Result<ArrayList<HashMap>> searchCapacity() {
        ArrayList<HashMap> hashMaps = sysCapacityService.searchCapacity();
        return Result.success(hashMaps);
    }


    /**
     * 修改
     *
     * @param code
     * @param value
     * @return
     */
    @PostMapping("updateCapacity")
    @OperationLog(DocLogsConstants.CAPACITY + DocLogsConstants.COMMON_UPDATE)
    public Result updateCapacity(String code, String value) {
        sysCapacityService.updateCapacity(code, value);
        return Result.success(true);
    }


}
