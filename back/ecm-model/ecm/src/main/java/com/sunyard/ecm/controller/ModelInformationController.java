package com.sunyard.ecm.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.dto.ecm.EcmDocDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.service.ModelInformationService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.DictionaryApi;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author： zyl
 * @create： 2023/4/13 9:56
 * @Desc: 建模管理-资料建模
 */
@Api(tags = "建模管理-资料建模")
@RestController
@RequestMapping("model/information")
public class ModelInformationController extends BaseController {
    @Resource
    private DictionaryApi dictionaryApi;
    @Resource
    private ModelInformationService modelInformationService;

    /**
     * 获取文件格式列表
     */
    @ApiOperation("获取文件格式列表")
    @OperationLog(LogsConstants.ROLES + "获取文件格式列表")
    @PostMapping("queryFileType")
    public Result queryFileType() {
        return dictionaryApi.getDictionaryAll(IcmsConstants.FILE_TYPE_DIC, null);
    }

    /**
     * 新增资料类型
     */
    @ApiOperation("新增资料类型")
    @OperationLog("新增资料类型")
    @PostMapping("addInformationType")
    public Result addInformationType(@RequestBody EcmDocDefDTO ecmDocDefExent) {
        return modelInformationService.addInformationType(ecmDocDefExent, getToken().getUsername());
    }

    /**
     * 编辑资料类型
     */
    @ApiOperation("编辑资料类型")
    @OperationLog("编辑资料类型")
    @PostMapping("editInformationType")
    public Result editInformationType(@RequestBody EcmDocDefDTO ecmDocDefExent) {
        return modelInformationService.editInformationType(ecmDocDefExent,
                getToken().getUsername());
    }

    /**
     * 删除资料类型
     */
    @ApiOperation("删除资料类型")
    @OperationLog("删除资料类型")
    @ApiImplicitParams({ @ApiImplicitParam(name = "docCode", value = "资料类型id", required = true), })
    @PostMapping("deleteInformationType")
    public Result deleteInformationType(@RequestBody EcmDocDefDTO ecmDocDefDTO) {
        return modelInformationService.deleteInformationType(ecmDocDefDTO);
    }

    /**
     * 查询单个资料类型详情
     */
    @ApiOperation("查询单个资料类型详情")
    @OperationLog("查询单个资料类型详情")
    @ApiImplicitParams({ @ApiImplicitParam(name = "docCode", value = "资料类型id", required = true),
            @ApiImplicitParam(name = "parentId", value = "父资料类型id", required = true),
            @ApiImplicitParam(name = "parentName", value = "父资料类型名称", required = true), })
    @PostMapping("searchInformationType")
    public Result<EcmDocDefDTO> searchInformationType(String docCode, String parentId,
                                                      String parentName) {
        return modelInformationService.searchInformationType(docCode, parentId, parentName);
    }

    /**
     * 查询资料类型树
     */
    @ApiOperation("查询资料类型树")
    @OperationLog("查询资料类型树")
    @PostMapping("searchInformationTypeTree")
    public Result<List<EcmDocTreeDTO>> searchInformationTypeTree(@RequestParam(required = false) String docCode) {
        return modelInformationService.searchInformationTypeTree(docCode);
    }

    /**
     * 查询资料类型树
     */
    @ApiOperation("查询父级目录树")
    @OperationLog("查询父级目录树")
    @PostMapping("searchInformationParentTypeTree")
    public Result<List<EcmDocTreeDTO>> searchInformationParentTypeTree(@RequestParam(required = false) String docCode) {
        return modelInformationService.searchInformationParentTypeTree(docCode);
    }

    /**
     * 拖拽修改资料类型树
     * 目前只是用两数除以2，会有问题，后续有时间要调整为单链表或双表分页结构（取修改和查询综合来看效率最高的方式）
     */
    @ApiOperation("拖拽修改资料类型树")
    @OperationLog("拖拽修改资料类型树")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sourceId", value = "要修改的资料类型id", required = true),
            @ApiImplicitParam(name = "sourceSort", value = "要修改的顺序号", required = true),
            @ApiImplicitParam(name = "upSort", value = "要修改上一个节点的顺序号", required = true),
            @ApiImplicitParam(name = "upSort", value = "要修改下一个节点的顺序号", required = true),
            @ApiImplicitParam(name = "sourceParentId", value = "要修改的父资料类型id", required = true),
            @ApiImplicitParam(name = "targetParentId", value = "新的父资料类型id", required = true),
            @ApiImplicitParam(name = "type", value = "0：业务树，1：资料树", required = true), })
    @PostMapping("dragTrees")
    public Result dragTree(String sourceId, Float sourceSort, Float upSort, Float downSort,
                           String sourceParentId, String targetParentId, Integer type) {
        return modelInformationService.dragTree(sourceId, sourceSort, upSort, downSort,
                sourceParentId, targetParentId, type, getToken());
    }

}
