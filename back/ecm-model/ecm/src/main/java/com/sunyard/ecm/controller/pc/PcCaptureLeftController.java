package com.sunyard.ecm.controller.pc;

import javax.annotation.Resource;

import com.sunyard.ecm.dto.redis.UserBusiRedisDTO;
import com.sunyard.framework.log.annotation.ApiLog;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.constant.StateConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.dto.ecm.ArchiveTreeQueryDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmStructureTreeDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.manager.DocMarkService;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.service.CaptureScanService;
import com.sunyard.ecm.vo.EcmDocMarkVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

/**
 * @author yzy
 * @desc 影像采集PC端页面-left区域
 * @since 2025/5/19
 */
@Api(tags = "影像采集PC端页面-left区域")
@RestController
@RequestMapping("pc/capture/left")
public class PcCaptureLeftController extends BaseController {

    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private CaptureScanService captureScanService;
    @Resource
    private DocMarkService docMarkService;
    @Resource
    private ModelBusiService modelBusiService;

    /**
     * 业务类型树(已过滤没权限的数据)
     */
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "业务类型树")
    @PostMapping("scanList/getAppTypeTreeFilterRight")
    public Result getAppTypeTreeFilterRight(String appCode) {
        return Result.success(modelBusiService.searchBusiTypeTree(appCode, getToken(), true, null));
    }

    /**
     * 业务类型树(已过滤没权限的数据)
     */
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "业务类型树")
    @PostMapping("getAppTypeTreeFilterRight")
    public Result getAppTypeTreeFilterRightRead(String appCode) {
        return Result
                .success(modelBusiService.searchBusiTypeTree(appCode, getToken(), true, "add"));
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

        return Result.success(operateCaptureService.getBusiStructureTree(getToken().getUsername(),
                ecmStructureTreeDTO, getToken()));
    }

    /**
     * 获取压缩包内文件树
     */
    @ApiOperation("获取压缩包内文件树")
    @OperationLog(LogsConstants.CAPTURE + "获取压缩包内文件树")
    @PostMapping("getArchiveStructureTree")
    public Result<List<EcmBusiStructureTreeDTO>> getArchiveStructureTree(@RequestBody ArchiveTreeQueryDTO params) {
        return Result.success(operateCaptureService.getArchiveStructureTree(params.getFileId(), getToken()));
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @OperationLog(LogsConstants.DOC_RIGHT + "删除")
    @PostMapping("/delete")
    public Result delete(Long busiId) {
        captureScanService.delete(busiId, getToken());
        return Result.success(true);
    }

    /**
     * 获取当前采集页面业务结构树里所展示的业务类型列表
     */
    @ApiOperation("获取当前采集页面业务结构树里所展示的业务类型列表")
    @OperationLog(LogsConstants.CAPTURE + "获取当前采集页面业务结构树里所展示的业务类型列表")
    @PostMapping("getAppTypeByCapture")
    public Result getAppTypeByCapture() {
        return Result.success(operateCaptureService.getAppTypeByCapture(getToken()));
    }

    /**
     * 获取业务类型的业务属性列表 返回前端展示直接识别类型
     */
    @ApiOperation("获取业务类型的业务属性列表")
    @OperationLog(LogsConstants.CAPTURE + "获取业务类型的业务属性列表")
    @PostMapping("getAppAttrList")
    public Result getAppAttrList(@RequestBody EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        return Result.success(operateCaptureService.getAppAttrList(ecmBusiInfoExtend.getAppCode(),
                ecmBusiInfoExtend.getBusiId(), getToken()));
    }

    /**
     * 添加业务
     */
    @ApiOperation("添加业务")
    @OperationLog(LogsConstants.CAPTURE + "添加业务")
    @LogManageAnnotation("新增业务")
    @PostMapping("addBusi")
    public Result addBusi(@RequestBody EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        return Result.success(operateCaptureService.addBusi(ecmBusiInfoExtend, getToken()));
    }

    /**
     * 编辑业务
     */
    @ApiOperation("编辑业务")
    @OperationLog(LogsConstants.CAPTURE + "编辑业务")
    @LogManageAnnotation("编辑业务")
    @PostMapping("editBusi")
    public Result editBusi(@RequestBody EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        operateCaptureService.editBusi(ecmBusiInfoExtend, getToken());
        return Result.success();
    }

    /**
     * 新增资料标记
     */
    @ApiOperation("新增资料标记")
    @OperationLog(LogsConstants.CAPTURE + "新增资料标记")
    @PostMapping("addDocMark")
    public Result addDocMark(@RequestBody EcmDocMarkVO vo) {
        return docMarkService.addDocMark(vo, getToken());
    }

    /**
     * 删除资料标记
     */
    @ApiOperation("删除资料标记")
    @OperationLog(LogsConstants.CAPTURE + "删除资料标记")
    @PostMapping("deleteDocMark")
    public Result deleteDocMark(@RequestBody EcmDocMarkVO vo) {
        vo.setCurrentUserId(getToken().getUsername());
        docMarkService.deleteDocMark(vo, getToken());
        return Result.success();
    }

    /**
     * 获取业务属性信息根据业务编号
     */
    @ApiOperation("获取业务属性信息根据业务编号")
    @OperationLog(LogsConstants.CAPTURE + "获取业务属性信息根据业务编号")
    @PostMapping("getBusiAttrInfo")
    public Result getBusiAttrInfo(String appCode, String busiNo) {
        return Result.success(operateCaptureService.getBusiAttrInfo(appCode, busiNo));
    }

    /**
     * 获取业务
     * @param
     * @return
     */
    @PostMapping("getBusiIdsByFlagId")
    @ApiOperation("获取业务列表")
    @ApiLog(LogsConstants.CAPTURE + "获取业务结构树")
    public Result getBusiIdsByFlagId() {
        UserBusiRedisDTO busiIdsByFlagId = operateCaptureService.getBusiIdsByFlagId(getToken());
        return Result.success(busiIdsByFlagId);
    }
}
