package com.sunyard.ecm.controller.api.pc;

import java.util.List;

import javax.annotation.Resource;

import com.sunyard.ecm.dto.ecm.EcmFileTagOperationHistoryDTO;
import com.sunyard.ecm.manager.FileTagOperationHistoryService;
import com.sunyard.ecm.po.EcmFileTagOperationHistory;
import com.sunyard.ecm.service.OperateExpireService;
import com.sunyard.ecm.vo.FileExpireVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.dto.ecm.EcmFileHistoryDTO;
import com.sunyard.ecm.dto.ecm.EcmFileLabelDto;
import com.sunyard.ecm.dto.ecm.EcmFileLabelExtendDTO;
import com.sunyard.ecm.dto.ecm.EcmIntelligentDetectionDTO;
import com.sunyard.ecm.dto.ecm.SysLabelTreeDTO;
import com.sunyard.ecm.manager.CaptureSubmitService;
import com.sunyard.ecm.manager.FileLabelService;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.manager.FileCommentService;
import com.sunyard.ecm.service.LogBusiService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.service.OperateQueryService;
import com.sunyard.ecm.service.SysLabelService;
import com.sunyard.ecm.service.CaptureScanService;
import com.sunyard.ecm.vo.BusiInfoVO;
import com.sunyard.ecm.vo.EcmBusiLogPrintDownVO;
import com.sunyard.ecm.vo.EcmCommentVO;
import com.sunyard.ecm.vo.EcmFileComHisResultVO;
import com.sunyard.ecm.vo.EcmFileCommentVO;
import com.sunyard.ecm.vo.EcmFileLableVO;
import com.sunyard.ecm.vo.EcmFileMarkCommentVO;
import com.sunyard.ecm.vo.EcmsCaptureVO;
import com.sunyard.ecm.vo.FileInfoRedisEntityVO;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.ecm.vo.MergFileVO;
import com.sunyard.ecm.vo.RotateFileVO;
import com.sunyard.ecm.vo.SplitFileVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.vo.FileEcmMergeVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author yzy
 * @desc 影像采集PC端页面-按钮区域
 * @since 2025/5/19
 */
@Api(tags = "影像采集PC端页面-按钮区域")
@RestController
@RequestMapping("api/pc/capture/button")
public class ApiPcCaptureButtonController extends BaseController {

    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private SysLabelService sysLabelService;
    @Resource
    private FileLabelService fileLabelService;
    @Resource
    private OperateQueryService operateQueryService;
    @Resource
    private LogBusiService logBusiService;
    @Resource
    private CaptureScanService captureScanService;
    @Resource
    private CaptureSubmitService captureSubmitService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private FileCommentService fileCommentService;
    @Resource
    private FileTagOperationHistoryService fileTagOperationHistoryService;
    @Resource
    private OperateExpireService operateExpireService;
    /**
     * 文件合并
     */
    @OperationLog(LogsConstants.ICMS + "文件合并")
    @PostMapping("mergeFile")
    public Result<SysFileDTO> mergeFile(@RequestBody FileEcmMergeVO ecmMergeVo) {
        return fileInfoService.mergeFile(ecmMergeVo, getToken());
    }

    /**
     * 合并文件前后插入文件信息
     */
    @ApiOperation("合并文件前后插入文件信息")
    @OperationLog(LogsConstants.ICMS + "合并文件前后插入文件信息")
    @PostMapping("mergInsertFileInfo")
    public Result mergInsertFileInfo(@RequestBody MergFileVO vo) {
        return fileInfoService.mergInsertFileInfo(vo, getToken());
    }

    /**
     * 影像删除
     */
    @ApiOperation("影像删除")
    @OperationLog(LogsConstants.CAPTURE + "影像删除")
    @PostMapping("deleteFileInfo")
    public Result deleteFileInfo(@RequestBody FileInfoVO vo) {
        fileInfoService.deleteFileInfo(vo, getToken());
        return Result.success(true);
    }

    /**
     * 编辑文件前后插入文件信息
     */
    @ApiOperation("编辑文件前后插入文件信息")
    @OperationLog(LogsConstants.ICMS + "编辑文件前后插入文件信息")
    @PostMapping("rotateInsertFileInfo")
    public Result rotateInsertFileInfo(@RequestBody RotateFileVO vo) {
        return fileInfoService.rotateInsertFileInfo(vo, getToken());
    }

    /**
     * 替换文件前后插入文件信息
     */
    @ApiOperation("编辑文件前后插入文件信息")
    @OperationLog(LogsConstants.ICMS + "替换文件前后插入文件信息")
    @PostMapping("replaceInsertFileInfo")
    public Result replaceInsertFileInfo(@RequestBody RotateFileVO vo) {
        return fileInfoService.replaceInsertFileInfo(vo, getToken());
    }

    /**
     * 文件拆分后插入文件信息
     */
    @ApiOperation("文件拆分后插入文件信息")
    @OperationLog(LogsConstants.ICMS + "文件拆分后插入文件信息")
    @PostMapping("splitFile")
    public Result splitFile(@RequestBody SplitFileVO vo) {
        return fileInfoService.splitFile(vo, getToken());
    }

    /**
     * 已删除列表中影像删除
     */
    @ApiOperation("已删除列表中影像删除")
    @OperationLog(LogsConstants.CAPTURE + "已删除列表中影像删除")
    @PostMapping("deleteFileInfoTrue")
    public Result deleteFileInfoTrue(@RequestBody FileInfoVO vo) {
        fileInfoService.deleteFileInfoTrue(vo, getToken());
        return Result.success(true);
    }

    /**
     * 影像恢复
     */
    @ApiOperation("影像恢复")
    @OperationLog(LogsConstants.CAPTURE + "影像恢复")
    @PostMapping("restoreFileInfo")
    public Result restoreFileInfo(@RequestBody FileInfoVO vo) {
        fileInfoService.restoreFileInfo(vo, getToken());
        return Result.success(true);
    }

    /**
     * 获取标签树结构
     */
    @ApiOperation("获取标签树结构")
    @OperationLog(LogsConstants.CAPTURE + "获取标签树结构")
    @PostMapping("getLabelStructureTree")
    public Result getLabelStructureTree() {
        List<SysLabelTreeDTO> labelTree = sysLabelService.getLabelStructureTree();
        return Result.success(labelTree);
    }

    /**
     * 获取文件关联标签
     */
    @ApiOperation("获取文件关联标签")
    @OperationLog(LogsConstants.CAPTURE + "获取文件关联标签")
    @PostMapping("getLabelByFileId")
    public Result getLabelByFileId(@RequestBody EcmFileLableVO ecmFileLableVO) {
        List<EcmFileLabelExtendDTO> ecmFileLabels = fileLabelService
                .getLabelByFileId(ecmFileLableVO);
        return Result.success(ecmFileLabels);
    }

    /**
     * 获取文件关联标签历史操作记录
     */
    @ApiOperation("获取文件关联标签历史操作记录")
    @OperationLog(LogsConstants.CAPTURE + "获取文件关联标签历史操作记录")
    @PostMapping("getFileLabelHistory")
    public Result getFileLabelHistory(@RequestBody EcmFileTagOperationHistoryDTO ecmFileLableVO) {
        List<EcmFileTagOperationHistory> historyList = fileTagOperationHistoryService
                .getFileLabelHistory(ecmFileLableVO);
        return Result.success(historyList);
    }

    /**
     * 新增或修改文件标签
     */
    @ApiOperation("新增或修改文件标签")
    @OperationLog(LogsConstants.CAPTURE + "新增或修改文件标签")
    @PostMapping("addOrUpdateFileLabel")
    public Result addOrUpdateFileLabel(@RequestBody EcmFileLabelDto ecmFileLabels) {
        fileLabelService.addOrUpdateFileLabel(ecmFileLabels,getToken());
        return Result.success();
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
        return Result.success();
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
     * 根据业务类型id获取业务列表
     */
    @ApiOperation("根据业务类型id获取业务列表")
    @OperationLog(LogsConstants.CAPTURE + "根据业务类型id获取业务列表")
    @PostMapping("getBusiInfoListByAppTypeId")
    public Result getBusiInfoListByAppTypeId(String appCode, Long busiId, String pageFlag) {
        return Result.success(captureSubmitService.getBusiInfoListByAppTypeId(appCode, busiId,
                pageFlag, getToken()));
    }

    /**
     * 影像文件归类
     */
    @ApiOperation("影像文件归类")
    @OperationLog(LogsConstants.CAPTURE + "影像文件归类")
    @PostMapping("classifyIcmsFile")
    public Result classifyIcmsFile(@RequestBody EcmsCaptureVO vo) {
        operateCaptureService.classifyIcmsFile(vo, getToken());
        return Result.success();
    }

    /**
     * 审核翻拍检测结果
     */
    @ApiOperation("更新审核检测结果")
    @OperationLog(LogsConstants.ICMS + "更新审核检测结果")
    @PostMapping("updateReviewDetection")
    public Result updateReviewDetection(@RequestBody EcmIntelligentDetectionDTO ecmIntelligentDetectionDTO) {
        return fileInfoService.updateReviewDetection(ecmIntelligentDetectionDTO);
    }

    /**
     * 获取翻拍检测结果
     */
    @ApiOperation("获取翻拍检测结果")
    @OperationLog(LogsConstants.ICMS + "获取翻拍检测结果")
    @PostMapping("getReviewDetection")
    public Result getReviewDetection(@RequestBody EcmIntelligentDetectionDTO ecmIntelligentDetectionDTO) {
        return fileInfoService.getReviewDetection(ecmIntelligentDetectionDTO);
    }

    /**
     * 新增批注
     */
    @ApiOperation("修改批注功能")
    @OperationLog(LogsConstants.CAPTURE + "修改批注功能")
    @PostMapping("addComment")
    public Result addComment(@RequestBody EcmCommentVO vo) {
        fileCommentService.addComment(vo, getToken());
        return Result.success();
    }

    /**
     * 获取批注
     */
    @ApiOperation("获取批注功能")
    @OperationLog(LogsConstants.CAPTURE + "获取批注功能")
    @PostMapping("getComment")
    public Result<List<EcmFileCommentVO>> getComment(@RequestBody EcmCommentVO vo) {
        return Result.success(fileCommentService.getComment(vo, getToken()));
    }

    /**
     * 新增评论
     */
    @ApiOperation("新增评论")
    @OperationLog(LogsConstants.CAPTURE + "新增评论")
    @PostMapping("addMarkComment")
    public Result addMarkComment(@RequestBody EcmFileMarkCommentVO vo) {
        fileCommentService.addMarkComment(vo, getToken());
        return Result.success();
    }

    /**
     * 获取批注记录
     */
    @ApiOperation("获取批注记录")
    @OperationLog(LogsConstants.CAPTURE + "获取批注记录")
    @PostMapping("getCommentHistory")
    public Result<List<EcmFileComHisResultVO>> getCommentHistory(@RequestBody EcmCommentVO vo) {
        return Result.success(fileCommentService.getCommentHistory(vo, getToken()));
    }

    /**
     * 复用弹框确认
     */
    @ApiOperation("复用弹框确认")
    @OperationLog(LogsConstants.CAPTURE + "复用弹框确认")
    @PostMapping("multiplexFile")
    public Result multiplexFile(@RequestBody FileInfoRedisEntityVO fileInfoRedisEntityVo) {
        return operateCaptureService.multiplexFile(fileInfoRedisEntityVo, getToken(), true);
    }

    /**
     * 复用弹框搜索
     */
    @ApiOperation("复用弹框搜索")
    @OperationLog(LogsConstants.CAPTURE + "复用弹框搜索")
    @PostMapping("multiplexFileSearch")
    public Result<PageInfo> multiplexFileSearch(@RequestBody BusiInfoVO busiInfoVo) {
        return Result.success(operateCaptureService.multiplexFileSearch(busiInfoVo, getToken()));
    }

    /**
     * 查询业务下所有文件名称
     */
    @ApiOperation("查询业务下所有文件名称")
    @OperationLog(LogsConstants.CAPTURE + "查询业务下所有文件名称")
    @PostMapping("searchFileNameList")
    public Result searchFileNameList(Long busiId, Integer type, String name, String docId) {
        return Result.success(
                captureSubmitService.searchFileNameList(busiId, type, name, docId, getToken()));
    }

    /**
     * 复用弹框搜索 业务类型下资料节点
     */
    @ApiOperation("复用弹框搜索-资料下拉列表(全部最子级)")
    @OperationLog(LogsConstants.CAPTURE + "复用弹框搜索-资料下拉列表(全部最子级)")
    @PostMapping("multiplexFileSearchDoc")
    public Result multiplexFileSearchDoc(@RequestBody BusiInfoVO busiInfoVo) {
        return operateCaptureService.multiplexFileSearchDoc(busiInfoVo, getToken());
    }

    /**
     * 文件还原
     */
    @ApiOperation("文件还原")
    @OperationLog(LogsConstants.QUERY + "文件还原")
    @PostMapping("restoreFile")
    public Result restoreFile(@RequestBody EcmFileHistoryDTO ecmFileHistoryDTO) {
        operateQueryService.restoreFile(ecmFileHistoryDTO, getToken());
        return Result.success(true);
    }

    /**
     * 影像文件期限设置
     */
    @ApiOperation("影像文件期限设置")
    @OperationLog(LogsConstants.CAPTURE + "影像文件期限设置")
    @PostMapping("setFileExpireDate")
    public Result setFileExpireDate(@RequestBody FileExpireVO vo) {
        operateExpireService.setFileExpireDate(vo);
        return Result.success();
    }

    /**
     * PDF文件拆分
     */
    @ApiOperation("PDF文件拆分")
    @OperationLog(LogsConstants.ICMS + "PDF文件拆分")
    @PostMapping("splitPdfToImage")
    public Result splitPdfToImage(@RequestBody SplitFileVO splitFileVO) {
        return fileInfoService.splitPdfToImage(splitFileVO, getToken());
    }

    /**
     * pdf 添加批注功能
     * @param splitFileVO
     * @return
     */
    @ApiOperation("PDF添加批注功能")
    @OperationLog(LogsConstants.CAPTURE + "PDF添加批注功能")
    @PostMapping("addPdfComment")
    public Result addPdfComment(@RequestBody SplitFileVO splitFileVO) {
        List<EcmCommentVO> vos = splitFileVO.getCommentList();
        for (EcmCommentVO vo : vos) {
            fileCommentService.addComment(vo, getToken());
        }
        return Result.success();
    }
}
