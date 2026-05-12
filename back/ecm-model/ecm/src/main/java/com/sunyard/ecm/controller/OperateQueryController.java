package com.sunyard.ecm.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import com.sunyard.ecm.dto.EcmAppAttrDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.service.CaptureScanService;
import com.sunyard.ecm.vo.BusiInfoVO;
import com.sunyard.ecm.vo.EcmStatisticsVO;
import com.sunyard.module.system.api.InstApi;
import com.sunyard.module.system.api.UserApi;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.annotation.TimeCalculateAnnotation;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.dto.ecm.EcmAppDocrightDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmStructureTreeDTO;
import com.sunyard.ecm.manager.CaptureSubmitService;
import com.sunyard.ecm.service.LogBusiService;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.service.OperateQueryService;
import com.sunyard.ecm.service.SysStrategyService;
import com.sunyard.ecm.vo.EcmBusiLogPrintDownVO;
import com.sunyard.ecm.vo.EcmsCaptureVO;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author： ty
 * @create： 2023/5/9 11:15
 * @Desc: 影像管理-影像查询
 */
@Api(tags = "影像管理-影像查询")
@TimeCalculateAnnotation
@RestController
@RequestMapping("operate/query")
public class OperateQueryController extends BaseController {

    @Resource
    private OpenApiService openApiService;
    @Resource
    private OperateQueryService operateQueryService;
    @Resource
    private LogBusiService logBusiService;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private SysStrategyService sysStrategyService;
    @Resource
    private CaptureSubmitService captureSubmitService;
    @Resource
    private CaptureScanService captureScanService;
    @Resource
    private InstApi instApi;
    @Resource
    private UserApi userApi;

    /** --------------------------------------- 影像查询 --------------------------------------- */
    /**
     * 业务类型树
     *
     */
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "业务类型树")
    @PostMapping("getAppTypeTree")
    public Result getAppTypeTree(@RequestBody EcmAppDocrightDTO appCode) {
        return Result.success(modelBusiService.searchBusiTypeTree(appCode.getAppCode(), getToken(),true,null));
    }

    /**
     * 查看影像文件信息 基本信息+EXIF
     */
    @ApiOperation("查看影像文件信息")
    @OperationLog(LogsConstants.QUERY + "查看影像文件信息")
    @PostMapping("getFileInfo")
    public Result getFileExifInfo(@RequestBody FileInfoVO fileInfoVO) {
        operateQueryService.getFileInfo(fileInfoVO.getBusiId(), fileInfoVO.getFileId(), getToken());
        return Result.success(true);
    }

    /**
     * 业务轨迹
     */
    @ApiOperation("业务轨迹")
    @OperationLog(LogsConstants.QUERY + "业务轨迹")
    @PostMapping("busiTrajectory")
    public Result busiTrajectory(@RequestBody FileInfoVO fileInfoVO) {
        return Result
                .success(operateQueryService.busiTrajectory(fileInfoVO.getBusiId(), getToken()));
    }

    /**
     * 添加影像日志记录
     */
    @ApiOperation("添加影像日志记录")
    @OperationLog(LogsConstants.CAPTURE + "添加添加影像日志记录")
    @PostMapping("addEcmLog")
    public Result addEcmLog(@RequestBody EcmBusiLogPrintDownVO vo) {
        logBusiService.addEcmLog(vo.getType(), vo.getFileIds(), vo.getBusiId(), getToken());
        return Result.success(true);
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
     * 获取业务结构树
     */
    @ApiOperation("获取业务结构树")
    @OperationLog(LogsConstants.CAPTURE + "获取业务结构树")
    @PostMapping("getBusiStructureTree")
    public Result getBusiStructureTree(@RequestBody EcmStructureTreeDTO ecmStructureTreeDTO) {
        if (ObjectUtils.isEmpty(ecmStructureTreeDTO.getIsDeleted())) {
            ecmStructureTreeDTO.setIsDeleted(StateConstants.ZERO);
        }

        List<EcmBusiStructureTreeDTO> busiStructureTree = operateCaptureService
                .getBusiStructureTree(getToken().getUsername(), ecmStructureTreeDTO, getToken());
        return Result.success(busiStructureTree);
    }

    /**
     * 影像文件列表
     */
    @ApiOperation("影像文件列表")
    @OperationLog(LogsConstants.CAPTURE + "影像文件列表")
    @LogManageAnnotation("查看业务")
    @PostMapping("searchEcmsFileList")
    public Result searchEcmsFileList(@RequestBody EcmsCaptureVO vo) {
        if (ObjectUtils.isEmpty(vo.getShowAll())) {
            vo.setShowAll(IcmsConstants.ZERO);
        }
        PageInfo pageInfo = operateCaptureService.searchEcmsFileList(vo, getToken());
        return Result.success(pageInfo);
    }

    /** --------------------------------------- 影像查看-筛选 --------------------------------------- */

    /**
     * 查询业务下所有文件名称
     * @param busiId 业务Id
     * @param type 0：文件名称，1：上传人
     * @param name 输入参数
     * @param docId 资料节点id
     */
    @ApiOperation("查询业务下所有文件名称")
    @OperationLog(LogsConstants.CAPTURE + "查询业务下所有文件名称")
    @PostMapping("searchFileNameList")
    public Result searchFileNameList(Long busiId, Integer type, String name, String docId) {
        return Result.success(
                captureSubmitService.searchFileNameList(busiId, type, name, docId, getToken()));
    }

    /**
     * 影像查询列表-获取业务属性搜索框
     */
    @ApiOperation("影像查询获取搜索框")
    @OperationLog(LogsConstants.DOC_RIGHT + "影像查询获取搜索框")
    @PostMapping("getSearchList")
    public Result getSearchList(@RequestBody List<String> appTypeIds) {
        return Result.success(captureScanService.getSearchList(appTypeIds, getToken()));
    }

    /**
     * 影像查询列表
     */
    @ApiOperation("影像查询列表")
    @OperationLog(LogsConstants.DOC_RIGHT + "影像查询列表")
    @PostMapping("searchList")
    public Result searchList(@RequestBody BusiInfoVO busiInfoVo) {
        return Result.success(captureScanService.searchList(busiInfoVo,getToken()));
    }

    /**
     * 属性按钮-获取属性信息
     */
    @ApiOperation("获取属性信息")
    @OperationLog(LogsConstants.CAPTURE + "获取属性信息")
    @PostMapping("getBusiAttrInfo")
    public Result getBusiAttrInfo(String appCode, Long busiId) {
        List<EcmAppAttrDTO> busiAttrInfo = captureScanService.getBusiAttrInfo(appCode, busiId,
                getToken());
        return Result.success(busiAttrInfo);
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
     * 影像扫描列表
     */
    @ApiOperation("影像业务状态列表")
    @OperationLog(LogsConstants.DOC_RIGHT + "影像业务状态列表")
    @PostMapping("searchBusiStatus")
    public Result searchBusiStatus(@RequestBody BusiInfoVO busiInfoVo) {
        return Result.success(captureScanService.searchBusiStatus(busiInfoVo,getToken()));
    }

    /**
     * 查询机构树
     */
    @ApiOperation("查询机构树")
    @OperationLog(LogsConstants.QUERY + "查询机构树")
    @PostMapping("searchInstTree")
    public Result searchInstTree() {
        return instApi.searchInstTree(getToken().getInstId());
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
     * 查询全局机构树-无权限
     */
    @ApiOperation("查询机构树")
    @OperationLog(LogsConstants.QUERY + "查询机构树")
    @PostMapping("searchInstTreeNoPermission")
    public Result searchInstTreeNoPermission() {
        return instApi.searchInstTree(null);
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
}
