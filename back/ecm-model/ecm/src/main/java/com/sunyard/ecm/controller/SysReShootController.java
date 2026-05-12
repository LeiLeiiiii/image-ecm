package com.sunyard.ecm.controller;

import java.util.List;

import javax.annotation.Resource;

import com.sunyard.ecm.service.SysReShootService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.manager.CommonService;
import com.sunyard.ecm.service.ModelInformationService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author ljw
 * @since: 2025/2/19
 * @Desc: 翻拍配置controller
 */
//todo 业务需抽离
@Api(tags = "系统管理-翻拍配置")
@RestController
@RequestMapping("sys/reShoot")
public class SysReShootController extends BaseController {

    @Resource
    private CommonService commonService;
    @Resource
    private ModelInformationService modelInformationService;
    @Resource
    private SysReShootService sysReShootService;

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
     * 查询资料类型树翻拍状态情况(0关闭,1开启)
     */
    @ApiOperation("查询资料类型树翻拍状态配置情况(0关闭,1开启)")
    @OperationLog("查询资料类型树翻拍状态配置情况(0关闭,1开启)")
    @PostMapping("searchStateTypeTreeIsRemade")
    public Result<List<EcmDocTreeDTO>> searchStateTypeTreeIsRemade(int state) {
        return sysReShootService.searchRemakeTypeTree(state);
    }

    /**
     * 更新翻拍配置开关状态
     */
    @ApiOperation("更新翻拍配置开关状态")
    @OperationLog("更新翻拍配置开关状态")
    @PostMapping("updateRemakeState")
    public Result updateRemakeState(@RequestBody List<EcmDocDef> vo) {
        return Result.success(sysReShootService.updateRemakeState(vo));
    }

    /**
     * 查询筛选翻拍数据
     */
    @ApiOperation("查询筛选翻拍数据")
    @OperationLog("查询筛选翻拍数据")
    @PostMapping("searchRemakeSift")
    public Result<List<EcmDocTreeDTO>> searchRemakeSift(@RequestBody EcmDocTreeDTO vo) {
        return commonService.searchSift(vo.getDocCodes(), vo.getTypeStates());
    }
}
