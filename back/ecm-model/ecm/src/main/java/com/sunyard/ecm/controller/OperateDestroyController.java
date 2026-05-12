package com.sunyard.ecm.controller;

import cn.hutool.core.lang.tree.Tree;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.dto.ecm.EcmAppDocrightDTO;
import com.sunyard.ecm.dto.ecm.EcmStructureTreeDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.OperateDestroyService;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.ecm.vo.DestroyInfoVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.InstApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


/**
 * @author ypy
 * @since 2025/7/2 13:50
 * @Desc: 影像管理-影像销毁
 */
@Api(tags = "影像管理-影像销毁")
@RestController
@RequestMapping("operate/destroy")
public class OperateDestroyController extends BaseController{

    @Resource
    private OperateDestroyService operateDestroyService;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private InstApi instApi;
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private OpenApiService openApiService;
    /**
     * 查询销毁任务
     */
    @ApiOperation("查询销毁任务")
    @OperationLog(LogsConstants.DESTROY + "查询销毁任务")
    @PostMapping("searchTask")
    public Result searchTask(@RequestBody DestroyInfoVO destroyInfoVO) {
        return operateDestroyService.searchTask(destroyInfoVO);
    }

    /**
     * 生成销毁清单
     */
    @ApiOperation("生成销毁清单")
    @OperationLog(LogsConstants.DESTROY + "生成销毁任务")
    @PostMapping("createDestroyList")
    public Result createDestroyList(@RequestBody DestroyInfoVO destroyInfoVO) {
        return operateDestroyService.createDestroyList(destroyInfoVO);
    }

    /**
     * 生成销毁任务
     */
    @ApiOperation("生成销毁任务")
    @OperationLog(LogsConstants.DESTROY + "生成销毁任务")
    @PostMapping("createDestroyTask")
    public Result createDestroyTask(@RequestBody DestroyInfoVO destroyInfoVO) {
        return operateDestroyService.createDestroyTask(destroyInfoVO,getToken());
    }

    /**
     * 审核销毁任务
     */
    @ApiOperation("审核销毁任务")
    @OperationLog(LogsConstants.DESTROY + "审核销毁任务")
    @PostMapping("auditTask")
    public Result auditTask(@RequestBody DestroyInfoVO destroyInfoVO) {
        return operateDestroyService.auditTask(destroyInfoVO,getToken());
    }

    /**
     * 撤销
     */
    @ApiOperation("撤销")
    @OperationLog(LogsConstants.DESTROY + "撤销")
    @PostMapping("cancel")
    public Result cancel(Long taskId) {
        return operateDestroyService.cancel(taskId);
    }

    /**
     * 审核销毁任务
     */
    @ApiOperation("审核")
    @OperationLog(LogsConstants.DESTROY + "审核")
    @PostMapping("audit")
    public Result audit(@RequestBody DestroyInfoVO destroyInfoVO) {
        return operateDestroyService.audit(destroyInfoVO,getToken());
    }

    /**
     * 销毁清册
     */
    @ApiOperation("销毁清册")
    @OperationLog(LogsConstants.DESTROY + "销毁清册")
    @PostMapping("destroyHistory")
    public Result destroyHistory(@RequestBody DestroyInfoVO destroyInfoVO) {
        return operateDestroyService.destroyHistory(destroyInfoVO);
    }

    /**
     * 业务类型树
     */
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.DESTROY + "业务类型树")
    @PostMapping("getAppTypeTree")
    public Result getAppTypeTree(@RequestBody EcmAppDocrightDTO appCode) {
        return Result.success(modelBusiService.searchBusiTypeTree(appCode.getAppCode(), getToken()));
    }

    /**
     * 查询机构树
     */
    @ApiOperation("查询机构树")
    @OperationLog(LogsConstants.DESTROY + "查询机构树")
    @PostMapping("searchInstTree")
    public Result searchInstTree() {
        return instApi.searchInstTree(null);
    }

    /**
     * 查询资料类型树
     */
    @ApiOperation("查询资料类型树")
    @OperationLog(LogsConstants.DESTROY + "查询资料类型树")
    @PostMapping("searchDocTypeTree")
    public Result<List<Tree<String>>> searchDocTypeTree() {
        return Result.success(operateFullQueryService.searchDocTypeTree(getToken(), ""));
    }

    /**
     * 跳转采集页面-单扫
     */
    @ApiOperation("跳转采集页面-单扫")
    @OperationLog(LogsConstants.DESTROY + "获取业务结构树")
    @PostMapping("singleCapture")
    public Result singleCapture(@RequestBody EcmStructureTreeDTO vo) {
        return Result.success(openApiService.singleCapture(vo, getToken()));
    }
}
