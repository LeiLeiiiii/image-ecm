package com.sunyard.ecm.controller;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.module.system.api.InstApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.dto.ecm.EcmAppDocrightDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.dto.redis.FileInfoRedisDTO;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.service.OperateFullQueryService;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.vo.EcmSearchVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import cn.hutool.core.lang.tree.Tree;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author zyl
 * @since 2023/8/2 13:50
 * @Desc: 影像管理-全文检索
 */
@Api(tags = "影像管理-全文检索")
@RestController
@RequestMapping("operate/fullQuery")
public class OperateFullQueryController extends BaseController{
    @Resource
    private OperateFullQueryService operateFullQueryService;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private InstApi instApi;
    /******************************* 公共查询 *******************************/
    /**
     * 全文检索
     */
    @ApiOperation("全文检索")
    @OperationLog(LogsConstants.ICMS + "全文检索")
    @PostMapping("search")
    public Result search(@RequestBody EcmSearchVO ecmSearchVO) {
        PageForm pageForm = new PageForm(ecmSearchVO.getPageNum(),ecmSearchVO.getPageSize());
        return operateFullQueryService.search(ecmSearchVO,pageForm,getToken());
    }

    /**
     * 标签检索
     */
    @ApiOperation("标签检索")
    @OperationLog(LogsConstants.ICMS + "标签检索")
    @PostMapping("searchLabel")
    public Result searchLabel(@RequestBody EcmSearchVO ecmSearchVO) {
        PageForm pageForm = new PageForm(ecmSearchVO.getPageNum(),ecmSearchVO.getPageSize());
        return operateFullQueryService.searchLabel(ecmSearchVO,pageForm,getToken());
    }

    /**
     * 标签检索
     */
    @ApiOperation("单证检索")
    @OperationLog(LogsConstants.ICMS + "单证检索")
    @PostMapping("searchDtd")
    public Result searchDtd(@RequestBody EcmSearchVO ecmSearchVO) {
        PageForm pageForm = new PageForm(ecmSearchVO.getPageNum(),ecmSearchVO.getPageSize());
        return operateFullQueryService.searchDtd(ecmSearchVO,pageForm,getToken(), IcmsConstants.ZERO);
    }

    /******************************* 文档查询 *******************************/

    /**
     * 查询资料类型树
     */
    @ApiOperation("查询资料类型树")
    @OperationLog(LogsConstants.ICMS + "查询资料类型树")
    @PostMapping("searchDocTypeTree")
    public Result<List<Tree<String>>> searchDocTypeTree(@RequestBody EcmSearchVO ecmSearchVO) {
        return Result.success(operateFullQueryService.searchDocTypeTree(getToken(),ecmSearchVO.getAppCode()));
    }

    /**
     * 查询文档类型
     */
    @ApiOperation("查询文档类型")
    @OperationLog(LogsConstants.ICMS + "查询文档类型")
    @PostMapping("searchDtdType")
    public Result searchDtdType(@RequestParam(value = "docCode", required = false) String docCode) {
        return Result.success(operateFullQueryService.searchDtdType(docCode));
    }

    /**
     * 查询文档类型属性
     */
    @ApiOperation("查询文档类型属性")
    @OperationLog(LogsConstants.ICMS + "查询文档类型属性")
    @PostMapping("searchDtdTypeAttr")
    public Result<HashMap<String, Object>> searchDtdTypeAttr(@RequestParam Long dtdTypeId) {
        return Result.success(operateFullQueryService.searchDtdTypeAttr(dtdTypeId,getToken()));
    }

    /**
     * 查询单个影像文件详情
     * @param busiId 业务id
     * @param fileId 文件id
     */
    @ApiOperation("查询单个影像文件详情")
    @OperationLog(LogsConstants.ICMS + "查询单个影像文件详情")
    @PostMapping("searchOneEcmsFile")
    public Result<FileInfoRedisDTO> searchOneEcmsFile(@RequestParam Long busiId, @RequestParam Long fileId) {
        return Result.success(operateFullQueryService.searchOneEcmsFile(busiId,fileId,getToken()));
    }

    /**
     * 业务类型树
     */
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "业务类型树")
    @PostMapping("getAppTypeTree")
    public Result getAppTypeTree(@RequestBody EcmAppDocrightDTO appCode) {
        return Result
                .success(modelBusiService.searchBusiTypeTree(appCode.getAppCode(), getToken(),true,null));
    }

    /**
     * 获取业务类型的业务属性列表 返回前端展示直接识别类型
     */
    @ApiOperation("获取业务类型的业务属性列表")
    @OperationLog(LogsConstants.CAPTURE + "获取业务类型的业务属性列表")
    @PostMapping("getAppAttrList")
    public Result getAppAttrList(@RequestBody EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        return Result.success(operateCaptureService.getAppAttrList(ecmBusiInfoExtend.getAppCode(),
                ecmBusiInfoExtend.getBusiId(), null));
    }
    @ApiOperation("查询机构树")
    @OperationLog(LogsConstants.STATISTICS + "查询机构树")
    @PostMapping("searchInstTree")
    public Result searchInstTree() {
        return instApi.searchInstTree(getToken().getInstId());
    }

}
