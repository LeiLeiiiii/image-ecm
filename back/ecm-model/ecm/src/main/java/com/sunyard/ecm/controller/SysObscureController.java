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
import com.sunyard.ecm.service.SysObscureService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author Wenbiwen
 * @since: 2025/2/25
 * @Desc 查重配置
 */
//todo 业务需抽离
@Api(tags = "系统管理-模糊配置")
@RestController
@RequestMapping("sys/obscure")
public class SysObscureController extends BaseController {

    @Resource
    private CommonService commonService;
    @Resource
    private ModelInformationService modelInformationService;
    @Resource
    private SysObscureService sysObscureService;
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
     * 查询资料类型树模糊状态情况(0关闭,1开启)
     */
    @ApiOperation("查询资料类型树模糊状态情况(0关闭,1开启)")
    @OperationLog("查询资料类型树模糊状态情况(0关闭,1开启)")
    @PostMapping("searchStateTypeTreeIsObscured")
    public Result<List<EcmDocTreeDTO>> searchStateTypeTreeIsObscured(int state) {
        return sysObscureService.searchObscuredTypeTree(state);
    }

    /**
     * 在模糊检测的基础上扩展反光缺角配置
     * @param vo
     * @return
     */
    @ApiOperation("更新质量检测状态")
    @OperationLog("更新质量检测状态")
    @PostMapping("updateObscuredState")
    public Result updateObscuredState(@RequestBody List<EcmDocDef> vo) {
        return Result.success(sysObscureService.updateObscuredState(vo));
    }

    /**
     * 查询筛选模糊数据
     */
    @ApiOperation("查询筛选模糊数据")
    @OperationLog("查询筛选模糊数据")
    @PostMapping("searchObscuredSift")
    public Result<List<EcmDocTreeDTO>> searchObscuredSift(@RequestBody EcmDocTreeDTO vo) {
        return commonService.searchSift(vo.getDocCodes(), vo.getTypeStates());
    }
}
