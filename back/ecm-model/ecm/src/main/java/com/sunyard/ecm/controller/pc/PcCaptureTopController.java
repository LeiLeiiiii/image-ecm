package com.sunyard.ecm.controller.pc;

import java.util.List;

import javax.annotation.Resource;

import com.sunyard.ecm.constant.IcmsConstants;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.dto.ecm.EcmBusiBatchScanDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiSingleScanDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.manager.CaptureSubmitService;
import com.sunyard.ecm.manager.BusiDealService;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.vo.EcmSubmitVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author yzy
 * @desc 影像采集页面PC端top区接口
 * @since 2025/5/19
 */
@Api(tags = "影像采集PC端页面-top区域")
@RestController
@RequestMapping("pc/capture/top")
public class PcCaptureTopController extends BaseController {

    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private CaptureSubmitService captureSubmitService;
    @Resource
    private BusiDealService busiDealService;

    /**
     * 自动归类检测列表
     */
    @ApiOperation("自动归类识别列表")
    @OperationLog(LogsConstants.ICMS + "自动归类识别列表")
    @PostMapping("getCheckAutoGroupList")
    public Result getCheckAutoGroupList(@RequestParam("busiId") Long busiId) {
        return fileInfoService.getCheckAutoGroupList(busiId);
    }

    /**
     * 修改用户自动归类识别开关
     */
    @ApiOperation("修改用户自动归类识别开关")
    @OperationLog(LogsConstants.ICMS + "修改用户自动归类识别开关")
    @PostMapping("updateCheckAutoUserSelf")
    public Result updateCheckAutoUserSelf() {
        return fileInfoService.updateCheckAutoUserSelf(getToken());
    }

    /**
     * 获取用户自动归类识别开关
     * @return
     */
    @ApiOperation("获取用户自动归类识别开关")
    @OperationLog(LogsConstants.ICMS + "获取用户自动归类识别开关")
    @PostMapping("getCheckAutoUserSelfFlag")
    public Result getCheckAutoUserSelfFlag() {
        return fileInfoService.getCheckAutoUserSelfFlag(getToken());
    }

    /**
     * 新增文件前后插入文件信息
     */
    @ApiOperation("新增文件前后插入文件信息")
    @OperationLog(LogsConstants.ICMS + "新增文件前后插入文件信息")
    @PostMapping("insertFileInfo")
    public Result insertFileInfo(@RequestBody EcmFileInfoDTO ecmFileInfoDTO) {
        ecmFileInfoDTO.setFileSource(IcmsConstants.FILE_SOURCE_PC);
        return fileInfoService.insertFileInfo(ecmFileInfoDTO, getToken());
    }

    /**
     * 影像提交
     */
    @ApiOperation("影像提交")
    @OperationLog(LogsConstants.CAPTURE + "影像提交")
    @PostMapping("submit")
    public Result submit(@RequestBody EcmSubmitVO ecmSubmitVO) {
        captureSubmitService.submit(getToken(), ecmSubmitVO);
        return Result.success();
    }

    /**
     * 扫码获取移动端页面路径
     */
    @ApiOperation("扫码获取移动端页面路径")
    @OperationLog(LogsConstants.MOBILE_CAPTURE + "获取移动端页面路径")
    @PostMapping("getMobilePagePath")
    public Result getMobilePagePath(@RequestBody List<Long> busiIdList) {
        return captureSubmitService.getMobilePagePath(busiIdList, getToken());
    }

    /**
     * 影像扫描-单扫
     */
    @ApiOperation("影像扫描-单扫")
    @OperationLog(LogsConstants.CAPTURE + "影像扫描-单扫")
    @PostMapping("singleScan")
    public Result scan(@RequestBody EcmBusiSingleScanDTO ecmBusiSingleScanDTO) {
        return Result.success(busiDealService.singleScan(ecmBusiSingleScanDTO, getToken()));
    }

    /**
     * 影像扫描-批扫
     */
    @ApiOperation("影像扫描-批扫")
    @OperationLog(LogsConstants.CAPTURE + "影像扫描-批扫")
    @PostMapping("batchScan")
    public Result batchScan(@RequestBody EcmBusiBatchScanDTO ecmBusiBatchScanDTO) {
        return Result.success(busiDealService.batchScan(ecmBusiBatchScanDTO, getToken()));
    }
}
