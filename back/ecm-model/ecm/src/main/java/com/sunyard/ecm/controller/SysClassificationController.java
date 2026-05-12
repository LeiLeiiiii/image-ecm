package com.sunyard.ecm.controller;

import java.util.List;

import javax.annotation.Resource;

import com.sunyard.ecm.service.SysClassificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.po.EcmDocDef;
import com.sunyard.ecm.manager.CommonService;
import com.sunyard.ecm.service.ModelInformationService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.dto.SysDictionaryDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author wenbiwen
 * @since: 2024/2/24
 * @Desc: 自动分类检测配置Controller
 */
//todo 业务需抽离
@Api(tags = "系统管理-分类配置")
@RestController
@RequestMapping("sys/classification")
public class SysClassificationController extends BaseController {

    @Resource
    private CommonService commonService;
    @Resource
    private SysClassificationService sysClassificationService;
    @Resource
    private ModelInformationService modelInformationService;

    /**
     * 查询资料类型树（包含自动分类检测标识）
     */
    @ApiOperation("查询资料类型树")
    @OperationLog("查询资料类型树")
    @PostMapping("searchInformationTypeTree")
    public Result<List<EcmDocTreeDTO>> searchInformationTypeTree() {
        return modelInformationService.searchInformationTypeTree(null);
    }

    /**
     * 查询资料类型树的自动分类检测状态（0关闭, 1开启）
     */
    @ApiOperation("查询资料类型树的自动分类检测状态（0关闭，1开启）")
    @OperationLog("查询资料类型树的自动分类检测状态")
    @PostMapping("searchStateTypeTreeIsAutoClassified")
    public Result<List<EcmDocTreeDTO>> searchStateTypeTreeIsAutoClassified(int state) {
        return sysClassificationService.searchAutoClassificationTypeTree(state);
    }

    /**
     * 更新自动分类检测状态
     */
    @ApiOperation("更新自动分类检测状态")
    @OperationLog("更新自动分类检测状态")
    @PostMapping("updateAutoClassificationState")
    public Result updateAutoClassificationState(@RequestBody List<EcmDocDef> vo) {
        return Result.success(sysClassificationService.updateAutoClassificationState(vo));
    }

    /**
     * 配置资料的自动分类标识
     */
    @ApiOperation("配置资料的自动分类标识")
    @OperationLog("配置资料的自动分类标识")
    @PostMapping("configureAutoClassificationSign")
    public Result<EcmDocDef> configureAutoClassificationSign(String docCode,
                                                             String autoClassificationId) {
        return sysClassificationService.configureAutoClassificationSign(docCode, autoClassificationId);

    }

    /**
     * 查询自动分类标识字典值集合
     */
    @ApiOperation("查询自动分类标识字典值集合")
    @OperationLog("查询自动分类标识字典值集合")
    @PostMapping("searchAutoClassificationSigns")
    public Result<List<SysDictionaryDTO>> searchAutoClassificationSigns() {
        return sysClassificationService.getAutoClassificationSigns();
    }

    /**
     * 查询筛选模糊数据
     */
    @ApiOperation("查询筛选自动分类数据")
    @OperationLog("查询筛选自动分类数据")
    @PostMapping("searchAutoClassificationSift")
    public Result<List<EcmDocTreeDTO>> searchAutoClassificationSift(@RequestBody EcmDocTreeDTO vo) {
        return commonService.searchSift(vo.getDocCodes(), vo.getTypeStates());
    }

    /**
     * 开关全部开或关
     */
    @ApiOperation("全部开关开启/关闭")
    @OperationLog("全部开关开启/关闭")
    @PostMapping("updateAllCmdState")
    public Result updateAllCmdState(@RequestParam("type") List<Integer> type,
                                    @RequestParam("state") Integer state) {
        return sysClassificationService.updateAllCmdState(type, state);
    }

}
