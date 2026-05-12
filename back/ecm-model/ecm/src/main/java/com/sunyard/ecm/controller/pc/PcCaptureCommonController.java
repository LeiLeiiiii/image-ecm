package com.sunyard.ecm.controller.pc;

import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.dto.ecm.SysDictionaryDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.service.SysStrategyService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.ApiLog;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.DictionaryApi;
import com.sunyard.module.system.api.MenuApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
/**
 * @author yzy
 * @desc
 * @since 2025/5/20
 */
@Api(tags = "影像采集PC端页面-公共接口")
@RestController
@RequestMapping("pc/capture/common")
public class PcCaptureCommonController extends BaseController {

    @Resource
    private MenuApi menuApi;
    @Resource
    private DictionaryApi dictionaryApi;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private SysStrategyService sysStrategyService;

    /**
     * 获取影像采集按钮列表
     */
    @ApiOperation("获取影像采集按钮列表")
    @OperationLog(LogsConstants.CAPTURE + "获取影像采集按钮列表")
    @PostMapping("getRightButtonList")
    public Result getRightButtonList() {
        return menuApi.getRightButtonListByMenuPerms(getToken().getId(),
                RoleConstants.IMAGE_CAPTURE);
    }

    /**
     * 获取影像查看功能按钮列表
     */
    @ApiOperation("获取影像查看功能按钮列表")
    @OperationLog(LogsConstants.CAPTURE + "获取影像查看功能按钮列表")
    @PostMapping("query/getRightButtonList")
    public Result getRightButtonListQuery() {
        return menuApi.getRightButtonListByMenuPerms(getToken().getId(), RoleConstants.IMAGE_VIEW);
    }

    /**
     * 获取全局压缩参数配置
     */
    @ApiOperation("获取全局压缩参数配置")
    @OperationLog("获取全局压缩参数配置")
    @PostMapping("getAppZipParams")
    Result<SysStrategyDTO> getAppZipParams() {
        return modelBusiService.getAppZipParams();
    }

    @ApiOperation("获取数据字典列表")
    @ApiLog(LogsConstants.ICMS + "获取数据字典列表")
    @PostMapping("selectValueByKey")
    public Result getDictionaryList(@RequestBody SysDictionaryDTO sysDictionaryDTO){
        return dictionaryApi.getDictionaryAll(sysDictionaryDTO.getKey(),sysDictionaryDTO.getSystemCode());
    }

    /**
     * 策略管理配置查询接口
     */
    @ApiOperation("策略管理配置查询接口")
    @OperationLog(LogsConstants.SYSTEM + "策略管理配置查询接口")
    @PostMapping("getUploadChunkSize")
    public Result getUploadChunkSize() {
        return operateCaptureService.getUploadChunkSize();
    }

    /**
     * 策略管理配置查询接口
     */
    @ApiOperation("策略管理配置查询接口")
    @OperationLog(LogsConstants.SYSTEM + "策略管理配置查询接口")
    @PostMapping("query")
    public Result queryConfig() {
        return Result.success(sysStrategyService.queryConfig());
    }

    /**
     * 获取影像采集按钮列表
     */
    @ApiOperation("获取影像采集按钮列表")
    @OperationLog(LogsConstants.CAPTURE + "获取影像采集按钮列表")
    @PostMapping("getDeletedButtonList")
    public Result getDeletedButtonList() {
        return menuApi.getRightButtonListByMenuPerms(getToken().getId(),
                RoleConstants.CAPTURE_VIEW_DELETED);
    }

    /**
     * 获取影像采集按钮列表
     */
    @ApiOperation("获取影像采集按钮列表")
    @OperationLog(LogsConstants.CAPTURE + "获取影像采集按钮列表")
    @PostMapping("query/getDeletedButtonList")
    public Result getDeletedButtonListQuery() {
        return menuApi.getRightButtonListByMenuPerms(getToken().getId(),
                RoleConstants.SEARCH_VIEW_DELETED);
    }

    @ApiOperation("查看文件记录日志")
    @PostMapping("viewFileLog")
    public void viewFileLog(Long busiId , String fileName, Integer size) {
        operateCaptureService.viewFileLog(busiId,fileName,size,getToken());
    }
}
