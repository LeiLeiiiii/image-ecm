package com.sunyard.ecm.controller;

import javax.annotation.Resource;

import com.sunyard.ecm.service.SysFileTypeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author ty
 * @since 2023-4-12 17:12
 * @Desc 系统管理-角色管理
 */
//todo 实现需要抽离
@Api(tags = "系统管理-文件格式配置")
@RestController
@RequestMapping("sys/fileType")
public class SysFileTypeController extends BaseController {
    @Resource
    private SysFileTypeService sysFileTypeService;

    /**
     * 获取文件格式列表
     */
    @ApiOperation("获取文件格式列表")
    @OperationLog(LogsConstants.ROLES + "获取文件格式列表")
    @PostMapping("queryFileType")
    public Result queryFileType(String limitFormat) {
        return sysFileTypeService.getFileTypeByDic(limitFormat);
    }

    /**
     * 更新文件格式
     */
    @ApiOperation("获取文件格式列表")
    @OperationLog(LogsConstants.ROLES + "获取文件格式列表")
    @PostMapping("updateFileType")
    public Result updateFileType(@RequestBody SysDictionaryDTO dto) {
        sysFileTypeService.updateFileType(dto);
        return Result.success(true);
    }

}
