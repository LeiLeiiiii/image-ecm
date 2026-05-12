package com.sunyard.ecm.controller.api.pc;

import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.annotation.TimeCalculateAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.dto.ecm.EcmStructureTreeDTO;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.vo.BusiInfoVO;
import com.sunyard.ecm.vo.EcmAIBridgeResultVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "对外接口-影像采集-影像扫描列表")
@RestController
@RequestMapping("api/operate/scan")
@TimeCalculateAnnotation
public class ApiPcOperateScanController extends BaseController {
    @Resource
    private OpenApiService openApiService;
    @Resource
    private ModelBusiService modelBusiService;

    /**
     * 跳转采集页面-单扫
     */
    @ApiOperation("跳转采集页面-单扫")
    @OperationLog(LogsConstants.CAPTURE + "获取业务结构树")
    @LogManageAnnotation("查看业务")
    @PostMapping("singleCapture")
    public Result singleCapture(@RequestBody EcmStructureTreeDTO vo) {
        return Result.success(openApiService.singleCapture(vo, getToken()));
    }

    /**
     * AI桥接-获取流程类型和业务类型树
     */
    @ApiOperation("AI桥接-获取流程类型和业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "AI桥接-获取流程类型和业务类型树")
    @PostMapping("getAIBridgeTypeTree")
    public Result<Map<String, EcmAIBridgeResultVO>> getAIBridgeTypeTree(
            @RequestBody BusiInfoVO busiInfoVO) {
        return Result.success(modelBusiService.getAIBridgeTypeTree(getToken(), busiInfoVO.getDelegateType(), busiInfoVO.getTypeBig()));
    }

    /**
     * AI桥接-获取流程类型和业务类型树
     */
    @ApiOperation("AI桥接-获取流程类型和业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "AI桥接-获取流程类型和业务类型树")
    @PostMapping("getAIBridgeTypeTreeCheck")
    public Result<Map<String, Boolean>> getAIBridgeTypeTreeCheck(
            @RequestBody BusiInfoVO busiInfoVO) {
        return Result.success(modelBusiService.getAIBridgeTypeTreeCheck(getToken(),busiInfoVO.getDelegateType(), busiInfoVO.getTypeBig()));
    }
}
