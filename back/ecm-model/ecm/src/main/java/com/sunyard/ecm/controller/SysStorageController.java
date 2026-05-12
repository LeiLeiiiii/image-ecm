package com.sunyard.ecm.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.dto.ecm.EcmBusiCacheStrategyDTO;
import com.sunyard.ecm.dto.ecm.EcmStorageQueDTO;
import com.sunyard.ecm.service.SysStorageService;
import com.sunyard.ecm.vo.EcmBusiCacheStrategyVO;
import com.sunyard.ecm.vo.EcmBusiMQListVO;
import com.sunyard.ecm.vo.EcmBusiStorageListVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author ty
 * @since 2023-4-12 17:12
 * @Desc 系统管理-存储管理
 */
@Api(tags = "系统管理-存储管理")
@RestController
@RequestMapping("sys/storage")
public class SysStorageController extends BaseController {

    @Resource
    private SysStorageService sysStorageService;

    /**
     * 从底座获取存储设备列表
     */
    @ApiOperation("获取存储设备列表")
    @OperationLog(LogsConstants.CAPTURE + "获取存储设备列表")
    @PostMapping("getStorageDeviceList")
    public Result<List<EcmStorageQueDTO>> getStorageDeviceList() {
        return sysStorageService.getStorageDeviceList();
    }

    /**
     * 获取业务存储列表
     */
    @ApiOperation("获取业务存储列表")
    @OperationLog(LogsConstants.CAPTURE + "getBusiStorageList")
    @PostMapping("getBusiStorageList")
    public Result<PageInfo<EcmBusiStorageListVO>> getBusiStorageList(PageForm page) {
        return Result.success(sysStorageService.getBusiStorageList(page));
    }

    /**
     * 新增业务关联存储及队列(新增业务时)
     */
    @ApiOperation("新增业务关联存储及队列")
    @OperationLog(LogsConstants.CAPTURE + "新增业务关联存储及队列")
    @PostMapping("addBusiRelationStorage")
    public Result addBusiRelationStorage(@RequestBody EcmBusiStorageListVO vo) {
        sysStorageService.addBusiRelationStorage(vo);
        return Result.success();
    }

    /**
     * 获取业务缓存策略
     */
    @ApiOperation("获取业务缓存策略")
    @OperationLog(LogsConstants.CAPTURE + "获取业务缓存策略")
    @PostMapping("getBusiCacheStrategy")
    public Result<EcmBusiCacheStrategyVO> getBusiStorageStrategy() {
        return Result.success(sysStorageService.getBusiStorageStrategy());
    }

    /**
     * 编辑业务缓存策略
     */
    @ApiOperation("编辑业务缓存策略")
    @OperationLog(LogsConstants.CAPTURE + "编辑业务缓存策略")
    @PostMapping("updateBusiCacheStrategy")
    public Result<EcmBusiCacheStrategyVO> updateBusiCacheStrategy(EcmBusiCacheStrategyDTO ecmBusiCacheStrategyDTO) {
        sysStorageService.updateBusiCacheStrategy(ecmBusiCacheStrategyDTO);
        return Result.success();
    }

    /**
     * 获取消息队列配置列表
     */
    @ApiOperation("获取消息队列配置列表")
    @OperationLog(LogsConstants.CAPTURE + "获取消息队列配置列表")
    @PostMapping("getMQSettingList")
    public Result<List<EcmStorageQueDTO>> getMQSettingList() {
        return Result.success(sysStorageService.getMQSettingList());
    }

    /**
     * 获取消息队到达通知策略列表
     */
    @ApiOperation("获取消息队到达通知策略列表")
    @OperationLog(LogsConstants.CAPTURE + "获取消息队到达通知策略列表")
    @PostMapping("getBusiMQList")
    public Result<PageInfo> getBusiMQList(PageForm page) {
        return Result.success(sysStorageService.getBusiMQList(page));
    }

    /**
     * 修改业务消息队列
     */
    @ApiOperation("修改业务消息队列")
    @OperationLog(LogsConstants.CAPTURE + "修改业务消息队列")
    @PostMapping("updateBusiMQ")
    public Result updateBusiMQ(@RequestBody EcmBusiMQListVO ecmBusiMQListVOS) {
        sysStorageService.updateBusiMQ(ecmBusiMQListVOS);
        return Result.success();
    }

    /**
     * 修改队列和业务关联的状态
     */
    @ApiOperation("修改队列和业务关联的状态")
    @OperationLog(LogsConstants.CAPTURE + "修改业务消息队列")
    @PostMapping("updateBusiMQStatus")
    public Result updateBusiMQStatus(@RequestBody EcmBusiMQListVO ecmBusiMQListVOS) {
        sysStorageService.updateBusiMQStatus(ecmBusiMQListVOS);
        return Result.success();
    }

}
