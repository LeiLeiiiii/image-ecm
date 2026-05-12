package com.sunyard.ecm.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.vo.EcmScanDownLoadVO;
import com.sunyard.ecm.vo.EcmStatisticsVO;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.MenuApi;
import com.sunyard.module.system.api.UserApi;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.annotation.TimeCalculateAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.dto.ecm.EcmAppDocrightDTO;
import com.sunyard.ecm.dto.ecm.EcmStructureTreeDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.service.CaptureScanService;
import com.sunyard.ecm.vo.BusiInfoVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.ecm.manager.StaticTreePermissService;
import com.sunyard.ecm.vo.EcmAIBridgeResultVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author XQZ
 * @date 2023/4/25
 * @Desc: 运营管理-影像采集-影像扫描列表
 */
@Api(tags = "运营管理-影像采集-影像扫描列表")
@TimeCalculateAnnotation
@RestController
@RequestMapping("operate/scan")
public class OperateScanController extends BaseController {
    @Resource
    private CaptureScanService captureScanService;
    @Resource
    private OpenApiService openApiService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private InstApi instApi;
    @Resource
    private UserApi userApi;
    @Resource
    private MenuApi menuApi;
    @Resource
    private StaticTreePermissService staticTreePermissService;
    /**
     * 业务类型树
     */
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "业务类型树")
    @PostMapping("getAppTypeTree")
    public Result getAppTypeTree(@RequestBody EcmAppDocrightDTO appCode) {
        return Result
                .success(modelBusiService.searchBusiTypeTree(appCode.getAppCode(), getToken(),true,null));
    }

    /**
     * 新增采集
     * @return 跳转采集地址
     */
    @ApiOperation("新增采集")
    @OperationLog(LogsConstants.CAPTURE + "新增采集")
    @PostMapping("addCapture")
    public Result addCapture(@RequestBody List<String> appTypeIds) {
        String url = operateCaptureService.addCapture(appTypeIds, getToken());
        return Result.success(url);
    }

    /**
     * 跳转采集页面-单扫
     */
    @ApiOperation("跳转采集页面-单扫")
    @OperationLog(LogsConstants.CAPTURE + "获取业务结构树")
    @LogManageAnnotation("查看业务")
    @PostMapping("singleCapture")
    public Result singleCapture(@RequestBody EcmStructureTreeDTO vo) {
        return Result.success(openApiService.singleCapture(vo, getToken()));
    }

    /**
     * 影像扫描列表-获取业务属性搜索框
     */
    @ApiOperation("影像采集获取搜索框")
    @OperationLog(LogsConstants.DOC_RIGHT + "影像采集获取搜索框")
    @PostMapping("getSearchList")
    public Result getSearchList(@RequestBody List<String> appTypeIds) {
        return Result.success(captureScanService.getSearchList(appTypeIds, getToken()));
    }

    /**
     * 业务类型树
     */
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "业务类型树")
    @PostMapping("getAppTypeTreeFilterRightAll")
    public Result getAppTypeTreeFilterRightAll(String appCode) {
        return Result
                .success(modelBusiService.searchBusiTypeTree(appCode, getToken(), false, "read"));
    }

    /**
     * 影像扫描列表
     */
    @ApiOperation("影像扫描列表")
    @OperationLog(LogsConstants.DOC_RIGHT + "影像扫描列表")
    @PostMapping("searchList")
    public Result searchList(@RequestBody BusiInfoVO busiInfoVo) {
        return Result.success(captureScanService.searchList(busiInfoVo,getToken()));
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @OperationLog(LogsConstants.DOC_RIGHT + "删除")
    @PostMapping("delete")
    public Result delete(Long busiId) {
        captureScanService.delete(busiId, getToken());
        return Result.success(true);
    }

    /**
     * 批量删除
     */
    @ApiOperation("批量删除")
    @OperationLog(LogsConstants.DOC_RIGHT + "批量删除")
    @PostMapping("deleteBatch")
    public Result deleteBatch(@RequestBody List<Long> busiIds) {
        captureScanService.deleteBatch(busiIds, getToken());
        return Result.success(true);
    }

    /**
     * 删除校验
     * @return 0：无影像文件，1：有影像文件
     */
    @ApiOperation("删除校验")
    @OperationLog(LogsConstants.DOC_RIGHT + "删除校验")
    @PostMapping("checkDelete")
    public Result checkDelete(Long busiId) {
        return Result.success(captureScanService.checkDelete(busiId, getToken()));
    }

    /**
     * 批量删除校验
     * @return 0：无影像文件，1：有影像文件
     */
    @ApiOperation("批量删除")
    @OperationLog(LogsConstants.DOC_RIGHT + "批量删除")
    @PostMapping("checkDeleteBatch")
    public Result checkDeleteBatch(@RequestBody List<Long> busiIds) {
        return Result.success(captureScanService.checkDeleteBatch(busiIds, getToken()));
    }

    /**
     * 获取页面唯一标识
     */
    @ApiOperation("获取页面唯一标识")
    @OperationLog(LogsConstants.CAPTURE + "获取页面唯一标识")
    @PostMapping("getPageFlag")
    public Result getPageFlag() {
        return Result.success(openApiService.scanOrUpdateEcmPc(getToken()));
    }

    /**
     * 影像扫描列表
     */
    @ApiOperation("影像业务状态列表")
    @OperationLog(LogsConstants.DOC_RIGHT + "影像业务状态列表")
    @PostMapping("searchBusiStatus")
    public Result searchBusiStatus(@RequestBody BusiInfoVO busiInfoVo) {
        return Result.success(captureScanService.searchBusiStatus(busiInfoVo,getToken()));
    }

    /**
     * 查询所有用户
     */
    @ApiOperation("查询所有用户")
    @OperationLog(LogsConstants.STATISTICS + "查询所有用户")
    @PostMapping("searchAllUser")
    public Result searchAllUser(EcmStatisticsVO vo) {
        if (!CollectionUtils.isEmpty(vo.getOrgCodes())) {
            ArrayList<Long> longs = new ArrayList<>();
            vo.getOrgCodes().forEach(s -> longs.add(Long.parseLong(s)));
            return userApi.getUsersByInstIdList(longs);
        }
        return userApi.getUsersByInstId(null);
    }

    /**
     * 查询机构树
     */
    @ApiOperation("查询机构树")
    @OperationLog(LogsConstants.STATISTICS + "查询机构树")
    @PostMapping("searchInstTree")
    public Result searchInstTree() {
        return instApi.searchInstTree(getToken().getInstId());
    }

    /**
     * 根据busIds批量下载业务文件
     */
    @ApiOperation("获取批量下载业务文件列表")
    @OperationLog(LogsConstants.STATISTICS + "获取批量下载业务文件列表")
    @PostMapping("getBatchDownloadBusiFile")
    public Result batchDownloadBusiFile(@RequestBody EcmScanDownLoadVO ecmScanDownLoadVO) {
        return captureScanService.batchDownloadBusiFile(ecmScanDownLoadVO, getToken());
    }

    /**
     * 根据busiId批量导出excel
     */
    @ApiOperation("批量导出业务影像扫描列表")
    @OperationLog(LogsConstants.STATISTICS + "批量导出业务影像扫描列表")
    @PostMapping("exportBusiListToExcel")
    public void getBusiExcel(@RequestBody List<Long> busiIds, HttpServletResponse response) {
        captureScanService.getBusiExcel(busiIds, getToken(), response);
    }

    /**
     * 批量新建树-获取下载模板
     */
    @ApiOperation("批量新建树-获取下载模板")
    @OperationLog(LogsConstants.STATISTICS + "批量新建树-获取下载模板")
    @PostMapping("exportTreesToExcel")
    public void exportTreesToExcel(@RequestBody EcmStatisticsVO vo, HttpServletResponse response) {
        captureScanService.exportTreesToExcel(vo, response);
    }

    /**
     * 批量导入功能
     */
    @ApiOperation("批量导入功能")
    @OperationLog(LogsConstants.STATISTICS + "批量导入功能")
    @PostMapping("parseExcelToBusi")
    public Result parseExcelToBusi(@RequestParam("file") MultipartFile file) {
        return operateCaptureService.parseExcelToBusi(file,getToken());
    }

    /**
     * 业务办结
     */
    @ApiOperation("业务办结")
    @OperationLog(LogsConstants.CAPTURE + "业务办结")
    @PostMapping("busiCompletion")
    public Result busiCompletion(@RequestBody List<Long> busiIds) {
        return captureScanService.busiCompletion(busiIds, getToken());
    }

    /**
     * 获取业务操作按钮列表
     * @return
     */
    @ApiOperation("业务操作按钮列表")
    @OperationLog(LogsConstants.CAPTURE + "业务操作按钮列表")
    @PostMapping("getRightButtonList")
    public Result getRightButtonList() {
        return menuApi.getRightButtonListByMenuPerms(getToken().getId(),
                RoleConstants.IMAGE_CAPTURE);
    }

    /**
     * 根据appCode列表查询资料类型
     */
    @ApiOperation("根据appCode列表查询资料类型")
    @OperationLog(LogsConstants.CAPTURE + "根据appCode列表查询资料类型")
    @PostMapping("getDocListByAppcode")
    public Result getDocListByAppcode(@RequestBody BusiInfoVO busiInfoVO) {
        return captureScanService.getDocListByAppcode(busiInfoVO.getAppCodes());
    }

    /**
     * 获取当前用户具有新增或修改权限的appCode列表
     */
    @ApiOperation("获取当前用户具有新增或修改权限的appCode列表")
    @OperationLog(LogsConstants.CAPTURE + "获取当前用户具有新增或修改权限的appCode列表")
    @PostMapping("getAppCodesWithAddOrUpdatePerm")
    public Result<List<String>> getAppCodesWithAddOrUpdatePerm() {
        List<String> codes = staticTreePermissService.getAppCodesWithAddOrUpdatePerm(getToken());
        return Result.success(codes);
    }

    /**
     * AI桥接-获取流程类型和业务类型树
     */
    @ApiOperation("AI桥接-获取流程类型和业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "AI桥接-获取流程类型和业务类型树")
    @PostMapping("getAIBridgeTypeTree")
    public Result<Map<String, EcmAIBridgeResultVO>> getAIBridgeTypeTree(
            @RequestBody BusiInfoVO busiInfoVO) {
        return Result.success(modelBusiService.getAIBridgeTypeTree(getToken(), busiInfoVO.getDelegateType(), busiInfoVO.getTypeBig()));
    }

    /**
     * AI桥接-获取流程类型和业务类型树
     */
    @ApiOperation("AI桥接-获取流程类型和业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "AI桥接-获取流程类型和业务类型树")
    @PostMapping("getAIBridgeTypeTreeCheck")
    public Result<Map<String, Boolean>> getAIBridgeTypeTreeCheck(
            @RequestBody BusiInfoVO busiInfoVO) {
        return Result.success(modelBusiService.getAIBridgeTypeTreeCheck(getToken(), busiInfoVO.getDelegateType(), busiInfoVO.getTypeBig()));
    }
}
