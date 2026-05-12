package com.sunyard.ecm.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.LogBusiService;
import com.sunyard.ecm.vo.EcmBusiLogPrintDownVO;
import com.sunyard.ecm.vo.SearchBusiLogVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author scm
 * @since 2023/7/31 17:52
 * @Desc 日志管理-业务日志
 */
@Api(tags = "日志管理-业务日志")
@RestController
@RequestMapping("log/busi")
public class LogBusiController extends BaseController {
    @Resource
    private LogBusiService logBusiService;
    @Resource
    private ModelBusiService modelBusiService;

    /**
     * 业务日志分页查询
     */
    @ApiOperation("业务日志查询")
    @OperationLog(LogsConstants.LOG + "业务日志查询")
    @PostMapping("queryBusiLog")
    public Result queryBusiLog(@RequestBody SearchBusiLogVO vo) {
        return Result.success(logBusiService.queryBusiLog(vo));
    }

    /**
     * 业务类型查询
     */
    @ApiOperation("业务类型查询")
    @OperationLog(LogsConstants.LOG + "业务类型查询")
    @PostMapping("queryAppType")
    public Result queryAppType(String appCode) {
        return Result.success(modelBusiService.searchBusiTypeTree(appCode, getToken()));
    }

    /**
     * 导出业务日志
     */
    @ApiOperation(value = "导出业务日志")
    @OperationLog(LogsConstants.LOG + "导出业务日志")
    @GetMapping("exportBusiLog")
    public void exportBusiLog(HttpServletResponse response, @RequestParam("logIds") List<Long> logIds) {
        logBusiService.exportBusiLog(response,logIds);
    }

    /**
     * 添加影像日志记录
     */
    @ApiOperation("添加影像日志记录")
    @OperationLog(LogsConstants.CAPTURE + "添加添加影像日志记录")
    @PostMapping("addEcmLog")
    public Result addEcmLog(@RequestBody EcmBusiLogPrintDownVO vo){
        logBusiService.addEcmLog(vo.getType(), vo.getFileIds(), vo.getBusiId(), getToken());
        return Result.success(true);
    }

    /**
     * 查询日志详情信息
     * @param ecmBusiLogId 影像操作日志主键id
     * @param operateContent 操作内容
     */
    @ApiOperation("查询日志详情信息")
    @OperationLog(LogsConstants.CAPTURE + "查询日志详情信息")
    @PostMapping("searchLogDetails")
    public Result searchLogDetails(Long ecmBusiLogId, String operateContent){
        return Result.success(logBusiService.searchLogDetails(ecmBusiLogId, operateContent,getToken()));
    }

}
