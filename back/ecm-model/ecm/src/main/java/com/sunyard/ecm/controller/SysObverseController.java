package com.sunyard.ecm.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.manager.CommonService;
import com.sunyard.ecm.service.ModelInformationService;
import com.sunyard.ecm.service.SysObverseService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ljw
 * @since: 2025/2/19
 * @Desc: 转正配置controller
 */
//todo 业务需抽离
@Slf4j
@Api(tags = "系统管理-转正配置")
@RestController
@RequestMapping("sys/regularize")
public class SysObverseController extends BaseController {

    @Resource
    private CommonService commonService;
    @Resource
    private ModelInformationService modelInformationService;
    @Resource
    private SysObverseService sysObverseService;

    /**
     * 查询资料类型树
     */
    @ApiOperation("查询资料类型树")
    @OperationLog("查询资料类型树")
    @PostMapping("searchStateTypeTree")
    public Result<List<EcmDocTreeDTO>> searchStateTypeTree() {
        return modelInformationService.searchInformationTypeTree(null);
    }

    /**
     * 查询资料类型树转正状态情况(0关闭,1开启)
     */
    @ApiOperation("查询资料类型树转正状态配置情况(0关闭,1开启)")
    @OperationLog("查询资料类型树转正状态配置情况(0关闭,1开启)")
    @PostMapping("searchStateTypeTreeIsRegularize")
    public Result<List<EcmDocTreeDTO>> searchStateTypeTreeIsRegularize(int state) {
        return sysObverseService.searchRegularizeTypeTree(state);
    }

    /**
     *更新转正配置状态
     */
    @ApiOperation("更新转正配置状态")
    @OperationLog("更新转正配置状态")
    @PostMapping("updateRegularizeState")
    public Result updateRegularizeState(@RequestBody List<EcmDocDef> vo) {
        return Result.success(sysObverseService.updateRegularizedState(vo));
    }

    /**
     * 查询筛选转正数据
     */
    @ApiOperation("查询筛选转正数据")
    @OperationLog("查询筛选转正数据")
    @PostMapping("searchRegularizeSift")
    public Result<List<EcmDocTreeDTO>> searchRegularizeSift(@RequestBody EcmDocTreeDTO vo) {
        return commonService.searchSift(vo.getDocCodes(), vo.getTypeStates());
    }
}
