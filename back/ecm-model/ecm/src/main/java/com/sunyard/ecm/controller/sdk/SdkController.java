package com.sunyard.ecm.controller.sdk;

import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.controller.BaseController;

import com.sunyard.ecm.dto.*;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.service.OperateCaptureService;

import com.sunyard.ecm.service.sdk.ApiAntiFraudService;
import com.sunyard.ecm.service.sdk.ApiBusiCopyService;
import com.sunyard.ecm.service.sdk.ApiBusiDeleteService;
import com.sunyard.ecm.service.sdk.ApiCaptureService;
import com.sunyard.ecm.service.sdk.ApiEditBusiStatusService;
import com.sunyard.ecm.service.sdk.ApiQueryBusiService;
import com.sunyard.ecm.service.sdk.ApiQueryService;
import com.sunyard.ecm.service.sdk.ApiSetBusiAttrService;
import com.sunyard.ecm.service.sdk.ApiUploadService;
import com.sunyard.ecm.vo.BusiDocDuplicateVO;
import com.sunyard.ecm.vo.EcmDelVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.ApiLog;
import com.sunyard.framework.log.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author
 * @since: 2023/8/14
 * @Desc: 对外接口
 */
@Api(tags = "对外接口")
@RestController
@RequestMapping("api")
public class SdkController extends BaseController {
    @Resource
    private ApiBusiCopyService apiBusiCopyService;
    @Resource
    private ApiCaptureService apiCaptureService;
    @Resource
    private ApiBusiDeleteService apiBusiDeleteService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private ApiQueryService apiQueryService;
    @Resource
    private ApiSetBusiAttrService apiSetBusiAttrService;
    @Resource
    private ApiEditBusiStatusService apiEditBusiStatusService;
    @Resource
    private ApiQueryBusiService apiQueryBusiService;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private BusiCacheService busiCacheService;
    @Resource
    private ApiUploadService apiUploadService;
    @Resource
    private ApiAntiFraudService apiAntiFraudService;

    /**
     * 业务建模-获取业务类型信息
     */
    @ApiOperation("获取业务类型信息")
    @ApiLog(LogsConstants.CAPTURE + "影像扫描-单扫")
    @PostMapping("upload/getAppTypeInfo")
    public Result getAppTypeInfo(@RequestBody EcmBusExtendDTO ecmBusExtendDTO) {
        return Result.success(busiCacheService.getRedisZip(ecmBusExtendDTO.getAppCode()));
    }

    /**
     * 校验文件是否允许上传
     */
    @ApiOperation("校验文件是否允许上传")
    @ApiLog(LogsConstants.OPEN + "校验文件是否允许上传")
    @PostMapping("upload/checkFile")
    public Result checkFile(@RequestBody EcmUploadAllDTO dto) {
        return apiUploadService.checkFile(dto);
    }

    /**
     * 新增文件前后插入文件信息
     */
    @ApiOperation("新增文件前后插入文件信息")
    @ApiLog(LogsConstants.ICMS + "新增文件前后插入文件信息")
    @PostMapping("upload/insertFileInfoBack")
    public Result insertFileInfo(@RequestBody EcmFileInfoApiDTO ecmFileInfoApiDTO) {
        return fileInfoService.insertFileInfoApi(ecmFileInfoApiDTO);
    }

    /**
     * 新增文件前后插入文件信息
     */
    @PostMapping("upload/insertFileListInfo")
    @ApiLog(LogsConstants.ICMS + "新增文件前后插入文件信息")
    public Result insertFileListInfo(@RequestBody EcmFileInfoApiDTO ecmFileInfoApiDTO) {
        return fileInfoService.insertFileListInfo(ecmFileInfoApiDTO);
    }
    /**
     * 业务属性回写
     */
    @ApiOperation("业务属性回写")
    @ApiLog(LogsConstants.OPEN + "业务属性回写")
    @LogManageAnnotation("编辑业务属性")
    @PostMapping("setBusiAttr/setBusiAttr")
    public Result setBusiAttr(@RequestBody EditBusiAttrDTO editBusiAttrDTO) {
        return apiSetBusiAttrService.setBusiAttr(editBusiAttrDTO);
    }
    /**
     * 影像查询
     */
    @ApiOperation("影像查询")
    @ApiLog(LogsConstants.OPEN + "影像查询")
    @PostMapping(value = "query/queryEcm", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Result queryAccessEcm(@RequestBody EcmRootDataDTO ecmRootDataDTO) {
        return Result.success(apiQueryService.queryData(ecmRootDataDTO));
    }
    /**
     * 获取业务或者资料节点对应的文件信息
     */
    @ApiOperation("获取业务或者资料节点对应的文件信息")
    @ApiLog(LogsConstants.OPEN + "获取业务或者资料节点对应的文件信息")
    @PostMapping("download/getFileInfoByBusiOrDoc")
    public Result getFileInfoByBusiOrDoc(@RequestBody EcmDownloadFileDTO ecmDownloadFileDTO) {
        return operateCaptureService.getFileInfoByBusiOrDoc(ecmDownloadFileDTO);
    }
    /**
     * 根据业务信息删除影像附件
     */
    @ApiOperation("根据业务信息删除影像附件")
    @ApiLog(LogsConstants.OPEN + "影像删除")
    @PostMapping("busiDelete/deleteFileByBusiOrDoc")
    public Result deleteFileByBusiOrDoc(@RequestBody EcmDelVO vo) {
        return apiBusiDeleteService.deleteFileByBusiOrDoc(vo);
    }
    /**
     * 影像复制接口
     */
    @ApiLog("影像复制接口")
    @OperationLog(LogsConstants.OPEN + "影像复制接口")
    @PostMapping("busiCopy/busiDocDuplicate")
    public Result busiDocDuplicate(@RequestBody BusiDocDuplicateVO busiDocDuplicateVo) {
        return apiBusiCopyService.busiDocDuplicate(busiDocDuplicateVo);
    }

    /**
     * 影像采集调阅
     *
     * @param vo
     * @return
     */
    @ApiOperation("影像采集调阅")
    @LogManageAnnotation("查看业务（对外接口）")
    @ApiLog(LogsConstants.OPEN + "影像采集调阅")
    @PostMapping(value = "capture/scanOrUpdateEcm", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Result scanOrUpdateEcm(@RequestBody EcmRootDataDTO vo) {
        return Result.success(apiCaptureService.businessDataService(vo));
    }

    /**
     * 业务属性回写
     */
    @ApiOperation("业务状态修改")
    @ApiLog(LogsConstants.OPEN + "修改业务状态")
    @LogManageAnnotation("修改业务状态")
    @PostMapping("editBusi/busiDeblock")
    public Result busiDeblock(@RequestBody EditBusiAttrDTO editBusiAttrDTO) {
        return apiEditBusiStatusService.busiDeblock(editBusiAttrDTO);
    }

    /**
     * 对外接口：文本查重
     */
    @ApiLog("文本查重对外接口")
    @OperationLog(LogsConstants.OPEN + "文本查重对外接口")
    @PostMapping("imageDup/extractFileTextDup")
    public Result extractFileTextDup(@RequestBody FileOcrCallBackDTO vo) {
        return apiAntiFraudService.extractFileTextDup(vo);
    }

    /**
     * 获取业务类型及文档列表
     */
    @ApiOperation("获取业务类型及文档列表")
    @ApiLog(LogsConstants.OPEN + "获取业务类型及文档列表")
    @LogManageAnnotation("获取业务类型及文档列表")
    @PostMapping("query/queryBusi")
    public Result queryBusi(@RequestBody QueryBusiDTO queryBusiDTO ) {
        return Result.success(apiQueryBusiService.queryBusi(queryBusiDTO));
    }

    /**
     * 自动归档接口
     */
    @ApiOperation("自动归档接口")
    @ApiLog(LogsConstants.OPEN + "自动归档接口")
    @LogManageAnnotation("自动归档接口")
    @PostMapping("busiCopy/busiArchive")
    public Result busiArchive(@RequestBody QueryBusiDTO queryBusiDTO ) {
        return Result.success(apiBusiCopyService.busiArchive(queryBusiDTO));
    }

    /**
     * 影像业务校验接口
     */
    @ApiOperation("影像业务校验")
    @ApiLog(LogsConstants.OPEN + "影像业务校验")
    @LogManageAnnotation("影像业务校验")
    @PostMapping(value = "check/ecmBusiInfoCheck", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Result ecmBusiInfoCheck(@RequestBody EcmBusiInfoDataDTO ecmBusiInfoDataDTO) {
        return apiQueryBusiService.ecmBusiInfoCheck(ecmBusiInfoDataDTO);
    }

    /**
     * 影像资料统计接口
     */
    @ApiOperation("影像资料统计")
    @ApiLog(LogsConstants.OPEN + "影像资料统计")
    @LogManageAnnotation("影像资料统计")
    @PostMapping(value = "statistics/statisticsDocFileNum", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Result statisticsDocFileNUm(@RequestBody EcmBusiInfoDataDTO ecmBusiInfoDataDTO) {
        return apiQueryService.statisticsDocFileNUm(ecmBusiInfoDataDTO);
    }
}
