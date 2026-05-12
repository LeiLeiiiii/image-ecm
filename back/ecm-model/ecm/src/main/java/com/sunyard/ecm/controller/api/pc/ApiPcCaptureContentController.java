package com.sunyard.ecm.controller.api.pc;

import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.manager.CaptureSubmitService;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.manager.AsyncTaskService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.vo.EcmAsyncTaskVO;
import com.sunyard.ecm.vo.EcmCommentVO;
import com.sunyard.ecm.vo.EcmsCaptureVO;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.ecm.vo.MoveFileVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.ObjectUtils;
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
@Api(tags = "影像采集PC端页面-内部显示区域")
@RestController
@RequestMapping("api/pc/capture/content")
public class ApiPcCaptureContentController extends BaseController {

    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private CaptureSubmitService captureSubmitService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private AsyncTaskService asyncTaskService;

    /**
     * 文件移动
     */
    @ApiOperation("文件移动")
    @OperationLog(LogsConstants.ICMS + "文件移动")
    @PostMapping("moveFile")
    public Result moveFile(@RequestBody MoveFileVO moveFileVo) {
        return fileInfoService.moveFile(moveFileVo, getToken());
    }

    /**
     * 修改备注和文件名
     */
    @ApiOperation("修改备注和文件名")
    @OperationLog(LogsConstants.CAPTURE + "修改备注和文件名")
    @PostMapping("updateFileNameAndComment")
    public Result updateFileNameAndComment(@RequestBody EcmCommentVO vo) {
        captureSubmitService.updateFileNameAndComment(vo, getToken());
        return Result.success();
    }

    /**
     * 影像文件列表
     */
    @ApiOperation("影像文件列表")
    @OperationLog(LogsConstants.CAPTURE + "影像文件列表")
    @PostMapping("searchEcmsFileList")
    public Result searchEcmsFileList(@RequestBody EcmsCaptureVO vo) {
        if (ObjectUtils.isEmpty(vo.getShowAll())) {
            vo.setShowAll(IcmsConstants.ZERO);
        }
        return Result.success(operateCaptureService.searchEcmsFileList(vo, getToken()));
    }

    /**
     * 影像待审核文件列表
     */
    @ApiOperation("影像待审核文件列表")
    @OperationLog(LogsConstants.CAPTURE + "影像待审核文件列表")
    @PostMapping("searchEcmsFileListInCheckFailed")
    public Result searchEcmsFileListInCheckFailed(@RequestBody EcmsCaptureVO vo) {
        if (ObjectUtils.isEmpty(vo.getShowAll())) {
            vo.setShowAll(IcmsConstants.ZERO);
        }
        return Result.success(operateCaptureService.searchEcmsFileListInCheckFailed(vo, getToken()));
    }

    /**
     * 影像文件重命名
     * 无需添加文件历史记录，无还原操作
     * (改进) 重命名改为备注
     */
    @ApiOperation("影像文件备注")
    @OperationLog(LogsConstants.CAPTURE + "影像文件备注")
    @PostMapping("updateFileName")
    public Result updateFileName(@RequestBody FileInfoVO vo) {
        vo.setCurentUserId(getToken().getUsername());
        vo.setCurentUserName(getToken().getName());
        operateCaptureService.updateFileName(vo);
        return Result.success();
    }

    /**
     * 获取异步任务栏列表
     */
    @ApiOperation("获取异步任务列表")
    @OperationLog(LogsConstants.CAPTURE + "获取异步任务栏列表")
    @PostMapping("getAsyncTaskList")
    public Result<EcmAsyncTaskVO> getAsyncTaskList(Long busiId) {
        return asyncTaskService.getEcmAsyncTaskList(busiId);
    }
}
