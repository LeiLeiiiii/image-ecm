package com.sunyard.ecm.controller.api.pc;

import com.sunyard.ecm.annotation.TimeCalculateAnnotation;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.dto.ecm.EcmAppDocrightDTO;
import com.sunyard.ecm.dto.redis.UserPageRedisDTO;
import com.sunyard.ecm.manager.CaptureSubmitService;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.service.CaptureScanService;
import com.sunyard.ecm.service.SysStrategyService;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.ApiLog;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.MenuApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author lw
 * @since: 2023/8/14
 * @Desc: 对外接口-影像采集调阅
 */
@Api(tags = "对外接口-影像采集调阅")
@RestController
@RequestMapping("api/capture")
@TimeCalculateAnnotation
public class ApiCaptureController extends BaseController {
    @Resource
    private MenuApi menuApi;
    @Resource
    private CaptureSubmitService captureSubmitService;
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private SysStrategyService sysStrategyService;
    @Resource
    private CaptureScanService captureScanService;
    @Resource
    private OperateCaptureService operateCaptureService;


    @PostMapping("getMenuRouters")
    @ApiOperation("获取菜单权限路由")
    @ApiLog(LogsConstants.ICMS + "获取菜单权限路由")
    public Result getMenuRouters(@RequestBody UserPageRedisDTO dto) {
        SysUserDTO sysUserDTO = new SysUserDTO();
        sysUserDTO.setRoleCode(getToken().getRoleCodeList());
        sysUserDTO.setMenuRootPerms(RoleConstants.SUNICMS);
        return menuApi.getMenuRoutersByRoot(sysUserDTO);
    }

    /**
     * 业务类型树
     *
     * @return
     */
    @PostMapping("getAppTypeTreeFilterRightAll")
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "业务类型树")
    public Result getAppTypeTreeFilterRightAll(String appCode) {
        return Result.success(modelBusiService.searchBusiTypeTree(appCode,getToken(),false,"read"));
    }

    /**
     * 策略管理配置查询接口
     * @return
     */
    //策略管理配置查询接口
    @PostMapping("query")
    @ApiOperation("策略管理配置查询接口")
    @ApiLog(LogsConstants.SYSTEM + "策略管理配置查询接口")
    public Result queryConfig() {
        return Result.success(sysStrategyService.queryConfig());
    }


    /** --------------------------------------- 影像采集 --------------------------------------- */
    /**
     * 删除业务类型节点
     *
     * @param appCode
     * @return
     */
    @PostMapping("deleteAppTypeNode")
    @ApiOperation("删除业务类型节点")
    @ApiLog(LogsConstants.CAPTURE + "删除业务类型节点")
    public Result deleteAppTypeNode(String appCode) {
        operateCaptureService.deleteAppTypeNode(appCode, getToken());
        return Result.success(true);
    }

    /** --------------------------------------- 在线预览 --------------------------------------- */

    /**
     * 在线预览
     *
     * @param fileId
     * @return
     */
    @GetMapping("onlineByFileId")
    @ApiOperation("在线预览")
    @ApiLog(LogsConstants.CAPTURE + "在线预览")
    public Result onlineByFileId(HttpServletResponse response, Long fileId) {
        operateCaptureService.onlineByFileId(response, fileId);
        return Result.success(true);
    }

    /** --------------------------------------- 影像提交 --------------------------------------- */
    /**
     * 获取页面业务类型
     */
    @PostMapping("getPageBusiType")
    @ApiOperation("获取页面业务类型")
    @ApiLog(LogsConstants.CAPTURE + "获取页面业务类型")
    public Result getPageBusiType(String busiId, String pageFlag) {
        return Result.success(captureSubmitService.getPageBusiType(busiId,pageFlag,getToken()));
    }

    /**
     * 获取页面唯一标识
     */
    @PostMapping("getPageFlag")
    @ApiOperation("获取页面唯一标识")
    @ApiLog(LogsConstants.CAPTURE + "获取页面唯一标识")
    public Result getPageFlag() {
        return Result.success(captureSubmitService.getPageFlag(getToken()));
    }


    /** --------------------------------------- 影像采集-筛选 --------------------------------------- */
    /**
     * 业务类型树
     *
     * @return
     */
    @PostMapping("getAppTypeTree")
    @ApiOperation("业务类型树")
    @OperationLog(LogsConstants.CAPTURE + "业务类型树")
    public Result getAppTypeTree(@RequestBody EcmAppDocrightDTO appCode) {
        return Result.success(modelBusiService.searchBusiTypeTree(appCode.getAppCode(), getToken()));
    }

    /**
     * 对外接口复用-获取业务属性搜索框
     * appTypeIds
     *
     * @return
     */
    @PostMapping("getSearchList")
    @ApiOperation("影像采集获取搜索框")
    @OperationLog(LogsConstants.DOC_RIGHT + "影像采集获取搜索框")
    public Result getSearchList(@RequestBody List<String> appTypeIds) {
        return Result.success(captureScanService.getSearchList(appTypeIds, getToken()));
    }

    /**
     * 根据nonce值换取flagId
     */
    @ApiOperation("根据url唯一属性换取flagId")
    @OperationLog(LogsConstants.CAPTURE + "根据url唯一属性换取flagId")
    @PostMapping("getFlagIdByUrlnonce")
    public Result getFlagIdByUrlnonce(@RequestParam("nonce") String nonce) {
        return Result.success(operateCaptureService.getFlagIdByUrlnonce(nonce));
    }

}
