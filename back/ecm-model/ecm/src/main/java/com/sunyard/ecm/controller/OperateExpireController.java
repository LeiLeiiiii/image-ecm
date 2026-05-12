package com.sunyard.ecm.controller;


import cn.hutool.core.lang.tree.Tree;
import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.dto.ecm.EcmAppDocrightDTO;
import com.sunyard.ecm.dto.ecm.EcmStructureTreeDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.OperateExpireService;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.ecm.vo.FileExpireVO;
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
import java.util.List;


/**
 * @author ypy
 * @since 2025/11/6 13:50
 * @Desc: 影像管理-影像期限
 */
@Api(tags = "影像管理-到期查询")
@RestController
@RequestMapping("operate/expire")
public class OperateExpireController extends BaseController {

    @Resource
    private OperateExpireService operateExpireService;
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private OpenApiService openApiService;

    /**
     * 查询到期文件
     */
    @ApiOperation("查询到期文件")
    @OperationLog(LogsConstants.EXPIRE + "查询到期文件")
    @PostMapping("searchExpireInfos")
    public Result searchExpireInfos(@RequestBody FileExpireVO vo) {
        return Result.success(operateExpireService.searchExpireInfos(vo,getToken()));
    }

    /**
     * 查询文档类型
     */
    @ApiOperation("查询文档类型")
    @OperationLog(LogsConstants.EXPIRE + "查询文档类型")
    @PostMapping("searchDtdType")
    public Result searchDtdType(@RequestParam(value = "docCode", required = false) String docCode) {
        return Result.success(operateFullQueryService.searchDtdType(docCode));
    }

    /**
     * 业务类型树
     */
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.EXPIRE + "业务类型树")
    @PostMapping("getAppTypeTree")
    public Result getAppTypeTree(@RequestBody EcmAppDocrightDTO appCode) {
        return Result
                .success(modelBusiService.searchBusiTypeTree(appCode.getAppCode(), getToken()));
    }

    /**
     * 跳转采集页面-单扫
     */
    @ApiOperation("跳转采集页面-单扫")
    @OperationLog(LogsConstants.EXPIRE + "获取业务结构树")
    @LogManageAnnotation("查看业务")
    @PostMapping("singleCapture")
    public Result singleCapture(@RequestBody EcmStructureTreeDTO vo) {
        return Result.success(openApiService.singleCapture(vo, getToken()));
    }

    /**
     * 查询资料类型树
     */
    @ApiOperation("查询资料类型树")
    @OperationLog(LogsConstants.EXPIRE + "查询资料类型树")
    @PostMapping("searchDocTypeTree")
    public Result<List<Tree<String>>> searchDocTypeTree() {
        return Result.success(operateFullQueryService.searchDocTypeTree(getToken(), ""));
    }
}
