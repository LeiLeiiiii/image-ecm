package com.sunyard.ecm.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.po.EcmSysLabel;
import com.sunyard.ecm.service.SysLabelService;
import com.sunyard.ecm.vo.SysLabelVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Wenbiwen
 * @Since  2025/2/18
 * @Desc 系统管理-标签管理
 */
@Api(tags = "系统管理-标签管理")
@RestController
@RequestMapping("sys/label")
public class SysLabelController extends BaseController {
    @Resource
    private SysLabelService sysLabelService;
    /**
     * 获取标签树结构
     */
    @OperationLog(LogsConstants.SYSTEM + "获取标签树结构")
    @ApiOperation("获取标签树结构")
    @PostMapping("getLabelStructureTree")
    public Result getLabelStructureTree() {
        return Result.success(sysLabelService.getLabelStructureTree());
    }

    /**
     * 获取标签详情
     * @param labelId 标签代码
     * @return 标签详情
     */
    @ApiOperation("获取标签详情")
    @OperationLog(LogsConstants.SYSTEM + "获取标签详情")
    @PostMapping("getLabelDetails")
    public Result getLabelDetails(Long labelId) {
        return sysLabelService.getLabelDetails(labelId);
    }

    /**
     * 新增标签
     * @param sysLabelVO 新增标签的数据
     * @return 操作结果
     */
    @ApiOperation("新增标签")
    @OperationLog(LogsConstants.SYSTEM + "新增标签")
    @PostMapping("addLabel")
    public Result addLabel(@RequestBody SysLabelVO sysLabelVO) {
        return sysLabelService.addLabel(sysLabelVO, getToken());
    }

    /**
     * 编辑标签
     * @param sysLabelVO 编辑标签的数据
     * @return 操作结果
     */
    @ApiOperation("编辑标签")
    @OperationLog(LogsConstants.SYSTEM + "编辑标签")
    @PostMapping("editLabel")
    public Result editLabel(@RequestBody SysLabelVO sysLabelVO) {
        return sysLabelService.editLabel(sysLabelVO, getToken());
    }

    /**
     * 删除单个标签
     * @param labelId 标签代码
     * @return 删除结果
     */
    @ApiOperation("删除单个标签")
    @OperationLog(LogsConstants.SYSTEM + "删除单个标签")
    @PostMapping("deleteLabel")
    public Result deleteLabel(@RequestBody EcmSysLabel labelId) {
        return sysLabelService.deleteLabel(labelId);
    }

}
