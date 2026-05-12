package com.sunyard.ecm.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmPlagiarismStateDTO;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.service.ModelInformationService;
import com.sunyard.ecm.service.SysDupService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author ljw
 * @since: 2025/2/21
 * @Desc: 查重配置controller
 */
@Api(tags = "系统管理-查重配置")
@RestController
@RequestMapping("sys/dup")
public class SysDupController extends BaseController {

    @Resource
    private SysDupService sysDupService;
    @Resource
    private ModelInformationService modelInformationService;

    /**
     * 查询全局配置
     */
    @ApiOperation("查询全局配置")
    @OperationLog("查询全局配置")
    @PostMapping("searGlobalplagiarism")
    public Result<EcmDocTreeDTO> searGlobalplagiarism() {
        return sysDupService.searGlobalplagiarism();
    }

    /**
     * 更新全局配置
     */
    @ApiOperation("更新全局配置")
    @OperationLog("更新全局配置")
    @PostMapping("updateGlobalplagiarism")
    public Result updateGlobalplagiarism(@RequestBody EcmDocTreeDTO dto) {
        return sysDupService.updateGlobalplagiarism(dto);
    }

    /**
     * 查询静态资料类型树
     */
    @ApiOperation("查询静态资料类型树")
    @OperationLog("查询静态资料类型树")
    @PostMapping("searchStateTypeTree")
    public Result<List<EcmDocTreeDTO>> searchStateTypeTree() {
        return modelInformationService.searchInformationTypeTree(null);
    }

    /**
     * 查询动态资料类型
     */
    @ApiOperation("查询动态资料")
    @OperationLog("查询动态资料")
    @PostMapping("searchDynaDoc")
    public Result<List<EcmDocTreeDTO>> searchDynaDoc() {
        return modelInformationService.searchDynamicPlagiarism();
    }

    /**
     * 查询全部资料类型，在查重策略资料类型中使用
     */
    @ApiOperation("查询全部资料")
    @OperationLog("查询全部资料")
    @PostMapping("searchAllDoc")
    public Result<List<EcmDocTreeDTO>> searchAllDoc() {
        return Result.success(sysDupService.searchAllDoc());
    }

    /**
     * 查询资料类型树查重状态情况(0关闭,1开启)
     */
    @ApiOperation("查询资料类型树查重状态配置情况(0关闭,1开启)")
    @OperationLog("查询资料类型树查重状态配置情况(0关闭,1开启)")
    @PostMapping("searchStateTypeTreeIsPlagiarism")
    public Result<List<EcmDocTreeDTO>> searchStateTypeTreeIsPlagiarism(int state) {
        return sysDupService.searchPlagiarismTypeTree(state);
    }

    /**
     * 更新静态资料查重配置
     */
    @ApiOperation("更新静态资料查重配置")
    @OperationLog("更新静态资料查重配置")
    @PostMapping("updateStaticPlagiarismState")
    public Result updateStaticPlagiarismState(@RequestBody List<EcmPlagiarismStateDTO> vo) {
        return Result.success(sysDupService.updateStaticPlagiarismState(vo));
    }

    /**
     * 更新静态资料查重配置
     */
    @ApiOperation("更新静态资料查重配置")
    @OperationLog("更新静态资料查重配置")
    @PostMapping("queryPlagiarismState")
    public Result queryPlagiarismState(String docCode, String type) {
        return Result.success(sysDupService.queryPlagiarismState(docCode, type));
    }

    /**
     * 保存动态资料查重配置
     */
    @ApiOperation("保存动态资料查重配置")
    @OperationLog("保存动态资料查重配置")
    @PostMapping("updateDynamicPlagiarismState")
    public Result updateDynamicPlagiarismState(@RequestBody List<EcmPlagiarismStateDTO> vo) {
        return Result.success(sysDupService.saveDynamicPlagiarismState(vo));
    }

    /**
     * 删除动态资料查重配置
     */
    @ApiOperation("删除动态资料查重配置")
    @OperationLog("删除动态资料查重配置")
    @PostMapping("deleteDynamicPlagiarism")
    public Result deleteDynamicPlagiarism(String docCode) {
        return Result.success(sysDupService.deleteDynamicPlagiarism(docCode));
    }

    /**
     * 更新静态资料开关
     */
    @ApiOperation("更新静态资料开关")
    @OperationLog("更新静态资料开关")
    @PostMapping("updateStaticState")
    public Result updateStaticState(@RequestBody List<EcmDocDef> vo) {
        return Result.success(sysDupService.updateStaticState(vo));
    }

    /**
     * 更新动态资料开关
     * @return
     */
    @ApiOperation("更新动态资料开关")
    @OperationLog("更新动态资料开关")
    @PostMapping("updateDynaState")
    public Result updateDynaState(String docCode, int state) {
        return Result.success(sysDupService.updateDynaState(docCode, state));
    }

    /**
     * 取消定制
     * @return
     */
    @ApiOperation("取消定制")
    @OperationLog("取消定制")
    @PostMapping("cancelCustomize")
    public Result cancelCustomize(String docCode, int type) {
        return Result.success(sysDupService.CancelCustomize(docCode, type));
    }

    /**
     * 查询筛选查重数据
     */
    @ApiOperation("查询筛选查重数据")
    @OperationLog("查询筛选查重数据")
    @PostMapping("searchPlagiarismSift")
    public Result<List<EcmDocTreeDTO>> searchPlagiarismSift(@RequestBody EcmDocTreeDTO vo) {
        return sysDupService.searchSift(vo.getDocCodes(), vo.getState(),vo.getTypeStates());
    }

}
