package com.sunyard.ecm.controller.api.pc;

import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.dto.ecm.EcmFileOcrInfoEsExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmIntelligentDetectionDTO;
import com.sunyard.ecm.manager.FileAttrOperationService;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.po.EcmDtdAttr;
import com.sunyard.ecm.po.EcmDtdDef;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.OperateQueryService;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.ecm.vo.OcrResultVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yzy
 * @desc 影像采集PC端页面-right详情区域
 * @since 2025/5/20
 */
@Api(tags = "影像采集PC端页面-right详情区域")
@RestController
@RequestMapping("api/pc/capture/right")
public class ApiPcCaptureRightController extends BaseController {

    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private OperateQueryService operateQueryService;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private FileAttrOperationService fileAttrOperationService;

    /**
     * 查看影像属性-识别（元数据管理）
     */
    @ApiOperation("查看影像属性-识别（元数据管理）")
    @OperationLog(LogsConstants.CAPTURE + "查看影像属性-识别（元数据管理）")
    @PostMapping("readImageAttr")
    public Result<EcmFileOcrInfoEsExtendDTO> readImageAttr(Long newFileId, Long busiId,
                                                           String docCode, Long fileId,
                                                           String appCode) {
        return Result.success(fileInfoService.readImageAttr(newFileId, busiId, docCode, fileId,
                appCode, getToken()));
    }

    /**
     * 查看影像属性-查看（元数据管理）
     */
    @ApiOperation("查看影像属性-查看（元数据管理）")
    @OperationLog(LogsConstants.CAPTURE + "查看影像属性-保存（元数据管理）")
    @PostMapping("searshImageAttr")
    public Result<EcmFileOcrInfoEsExtendDTO> searshImageAttr(Long fileId) {
        return Result.success(fileInfoService.searshImageAttr(fileId));
    }

    /**
     * 查看影像属性-保存（元数据管理）
     */
    @ApiOperation("查看影像属性-保存（元数据管理）")
    @OperationLog(LogsConstants.CAPTURE + "查看影像属性-保存（元数据管理）")
    @PostMapping("saveImageAttr")
    public Result readImageAttr(@RequestBody OcrResultVO vo) {
        fileInfoService.saveImageAttr(vo, getToken());
        return Result.success(true);
    }

    /**
     * 查询文件影像属性操作记录
     * @param fileId
     * @return
     */
    @ApiOperation("查询文件影像属性操作记录")
    @OperationLog(LogsConstants.CAPTURE + "查询文件影像属性操作记录")
    @PostMapping("file/attr/operations")
    public Result<Map<Long, List<Map<String, Object>>>> queryOperationsByFileId(@RequestParam String fileId) {
        // 调用服务层方法获取分组后的操作记录
        Map<Long, List<Map<String, Object>>>  resultMap = fileAttrOperationService.queryOperationsGroupByDtdTypeId(fileId);
        return Result.success(resultMap);
    }

    /**
     * 查看影像属性-查看（元数据管理）
     */
    @ApiOperation("查看影像属性-文档类型列表（元数据管理）")
    @OperationLog(LogsConstants.CAPTURE + "查看影像属性-文档类型列表")
    @PostMapping("searchFileAttrType")
    public Result<List<EcmDtdDef>> searchFileAttrType(@RequestBody OcrResultVO vo) {
        return Result.success(fileInfoService.searchFileAttrType(vo, getToken()));
    }

    /**
     * 查看影像属性-查看（元数据管理）
     */
    @ApiOperation("查看影像属性-选择文档类型后的属性展示")
    @OperationLog(LogsConstants.CAPTURE + "查看影像属性-保存（元数据管理）")
    @PostMapping("getOcrAttrList")
    public Result<List<EcmDtdAttr>> getOcrAttrList(Long dtdTypeId) {
        return Result.success(fileInfoService.getOcrAttrList(dtdTypeId, getToken()));
    }

    /**
     * 重试检测任务
     */
    @ApiOperation("重试检测任务")
    @OperationLog(LogsConstants.ICMS + "重试检测任务")
    @PostMapping("retryTask")
    public Result retryTask(@RequestBody EcmIntelligentDetectionDTO retryDTO) {
        fileInfoService.retryTask(retryDTO, getToken());
        return Result.success("操作成功");
    }

    /**
     * 影像文件历史
     */
    @ApiOperation("影像文件历史")
    @OperationLog(LogsConstants.QUERY + "影像文件历史")
    @PostMapping("getFileHistory")
    public Result getFileHistory(@RequestBody FileInfoVO vo) {
        return Result.success(operateQueryService.getFileHistory(vo, getToken()));
    }

    /**
     * 获取文件智能化处理状态
     */
    @ApiOperation("获取文件智能化处理状态")
    @OperationLog("获取文件智能化处理状态")
    @PostMapping("getProcessingStatus")
    public Result<List<Map<String, Object>>> getProcessingStatus(Long fileId) {
        return modelBusiService.getProcessingStatus(fileId);
    }

    /**
     * 获取查重结果
     */
    @ApiOperation("获取查重结果")
    @OperationLog(LogsConstants.ICMS + "获取查重结果")
    @PostMapping("getNoteRes")
    public Result getNoteRes(@RequestBody EcmIntelligentDetectionDTO ecmIntelligentDetectionDTO) {
        return fileInfoService.getNoteRes(getToken(), ecmIntelligentDetectionDTO);
    }

    /**
     * 获取文件exif信息
     */
    @ApiOperation("获取文件exif信息")
    @OperationLog("获取文件exif信息")
    @PostMapping("getFileExifFromES")
    Result<HashMap> getFileExifFromES(Long fileId) {
        return modelBusiService.getFileExifFromES(fileId);
    }

    /**
     * 获取查重权限
     * @param ecmIntelligentDetectionDTO
     * @return
     */
    @ApiOperation("获取查重权限")
    @OperationLog(LogsConstants.ICMS + "查重比对：获取权限")
    @PostMapping("getDocRight")
    public Result getDocRight(@RequestBody EcmIntelligentDetectionDTO ecmIntelligentDetectionDTO) {
        return fileInfoService.addDocRight(getToken(), ecmIntelligentDetectionDTO);
    }
}
