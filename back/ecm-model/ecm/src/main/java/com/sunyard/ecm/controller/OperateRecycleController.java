package com.sunyard.ecm.controller;

import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.dto.ecm.EcmAppDocrightDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiRecycleDelDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiRecycleRestoreDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.OperateRecycleService;
import com.sunyard.ecm.service.CaptureScanService;
import com.sunyard.ecm.vo.EcmBusiRecycleSearchVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 运营管理-回收站
 *
 * @author wzz
 * @since 2024/6/4 17:28
 * @DESC：影像管理-回收站
 */
@Api(tags = "影像管理-回收站")
@RestController
@RequestMapping("operate/recycle")
public class OperateRecycleController extends BaseController {
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private CaptureScanService captureScanService;
    @Resource
    private OperateRecycleService operateRecycleService;
    @Resource
    private OpenApiService openApiService;
    /**
     * 业务类型树
     */
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.RECYCLE_BIN + "业务类型树")
    @PostMapping("getAppTypeTree")
    public Result getAppTypeTree(@RequestBody EcmAppDocrightDTO appCode) {
        return Result.success(modelBusiService.searchBusiTypeTree(appCode.getAppCode(), getToken()));
    }

    /**
     * 获取业务属性搜索框
     */
    @ApiOperation("获取搜索框")
    @OperationLog(LogsConstants.RECYCLE_BIN + "获取搜索框")
    @PostMapping("getSearchList")
    public Result getSearchList(@RequestBody List<String> appTypeIds) {
        return Result.success(captureScanService.getSearchList(appTypeIds, getToken()));
    }

    /**
     * 回收站业务列表
     */
    @ApiOperation("影像回收站业务列表")
    @OperationLog(LogsConstants.RECYCLE_BIN + "影像回收站业务列表")
    @PostMapping("searchList")
    public Result searchList(@RequestBody EcmBusiRecycleSearchVO ecmBusiRecycleSearchVO) {
        return operateRecycleService.search(ecmBusiRecycleSearchVO, getToken());
    }

    /**
     * 单笔业务删除
     */
    @ApiOperation("影像回收站单笔业务删除")
    @OperationLog(LogsConstants.RECYCLE_BIN + "影像回收站单笔业务删除")
    @PostMapping("delete")
    public Result delete(@RequestBody EcmBusiRecycleDelDTO ecmBusiRecycleDelDTO) {
        return operateRecycleService.del(ecmBusiRecycleDelDTO, getToken());
    }

    /**
     * 回收站业务批量删除
     *
     */
    @ApiOperation("影像回收站业务批量删除")
    @OperationLog(LogsConstants.RECYCLE_BIN + "影像回收站业务批量删除")
    @PostMapping("deleteBatch")
    public Result deleteBatch(@RequestBody List<EcmBusiRecycleDelDTO> ecmBusiRecycleDelDTOList) {
        return operateRecycleService.delBatch(ecmBusiRecycleDelDTOList, getToken());
    }

    /**
     * 单笔业务恢复
     */
    @ApiOperation("影像回收站单笔业务恢复")
    @OperationLog(LogsConstants.RECYCLE_BIN + "影像回收站单笔业务恢复")
    @PostMapping("restore")
    public Result restore(@RequestBody EcmBusiRecycleRestoreDTO ecmBusiRecycleRestoreDTO) {
        return operateRecycleService.restore(ecmBusiRecycleRestoreDTO, getToken());
    }

    /**
     * 业务批量恢复
     */
    @ApiOperation("影像回收站业务批量恢复")
    @OperationLog(LogsConstants.RECYCLE_BIN + "影像回收站业务批量恢复")
    @PostMapping("restoreBatch")
    public Result restoreBatch(@RequestBody List<EcmBusiRecycleRestoreDTO> ecmBusiRecycleRestoreDTOList) {
        return operateRecycleService.restoreBatch(ecmBusiRecycleRestoreDTOList, getToken());
    }

    /**
     * 查看业务信息
     */
    @ApiOperation("查看业务信息")
    @LogManageAnnotation("查看回收站业务")
    @PostMapping("getRecycleBusiInfo")
    public Result getRecycleBusiInfo(Long busiId) {
        return Result.success(captureScanService.getRecycleBusiInfo(busiId));
    }

    /**
     * 查看文件信息
     */
    @ApiOperation("查看文件信息")
    @LogManageAnnotation("查看回收站业务文件")
    @PostMapping("getRecycleFileInfo")
    public Result getRecycleFileInfo(Long busiId, PageForm pageForm) {
        return Result.success(captureScanService.getRecycleFileInfo(busiId , pageForm));
    }
}
