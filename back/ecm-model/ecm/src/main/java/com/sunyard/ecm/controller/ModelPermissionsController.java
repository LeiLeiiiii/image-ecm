package com.sunyard.ecm.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.dto.ecm.EcmGetRoleListDTO;
import com.sunyard.ecm.po.EcmDimensionDef;
import com.sunyard.ecm.service.ModelInformationService;
import com.sunyard.ecm.service.ModelPermissionsService;
import com.sunyard.ecm.vo.AddVerVO;
import com.sunyard.ecm.vo.DocRightRoleAndLotVO;
import com.sunyard.ecm.vo.DocRightVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.system.api.dto.SysRoleDTO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author ty
 * @Desc: 建模管理-资料权限
 * @since 2023-4-17 11:09
 */
@Api(tags = "建模管理-资料权限")
@RestController
@RequestMapping("model/permissions")
public class ModelPermissionsController extends BaseController {
    @Resource
    private ModelPermissionsService modelPermissionsService;
    @Resource
    private ModelInformationService modelInformationService;

    /**
     * 业务权限版本管理列表
     */
    @ApiOperation("业务权限版本管理列表")
    @OperationLog(LogsConstants.DOC_RIGHT + "业务权限版本管理列表")
    @PostMapping("getRightVerList")
    public Result getRightVerList(@RequestBody AddVerVO addVerVo) {
        return Result.success(modelPermissionsService.getRightVerList(addVerVo));
    }

    /**
     * 获取版本号
     */
    @ApiOperation("获取版本号")
    @OperationLog(LogsConstants.DOC_RIGHT + "获取版本号")
    @PostMapping("getVerNo")
    public Result getVerNo(String appCode) {
        return Result.success(modelPermissionsService.getVerNo(appCode));
    }

    /**
     * 新增版本
     */
    @ApiOperation("新增版本")
    @OperationLog(LogsConstants.DOC_RIGHT + "新增版本")
    @PostMapping("addVer")
    public Result addVer(@RequestBody AddVerVO addVerVo) {
        addVerVo.setCreateUser(getToken().getUsername());
        return Result.success(modelPermissionsService.addVer(addVerVo));
    }

    /**
     * 设为当前版本
     */
    @ApiOperation("设为当前版本")
    @OperationLog(LogsConstants.DOC_RIGHT + "设为当前版本")
    @PostMapping("setRightNew")
    public Result setRightNew(Long id, String appCode) {
        modelPermissionsService.setRightNew(id, appCode, getToken().getUsername());
        return Result.success(true);
    }

    /**
     * 版本编辑
     */
    @ApiOperation("版本编辑")
    @OperationLog(LogsConstants.DOC_RIGHT + "版本编辑")
    @PostMapping("editVer")
    public Result editVer(@RequestBody AddVerVO addVerVo) {
        addVerVo.setUpdateUser(getToken().getUsername());
        modelPermissionsService.editVer(addVerVo);
        return Result.success(true);
    }

    /**
     * 版本编辑
     */
    @ApiOperation("删除版本")
    @OperationLog(LogsConstants.DOC_RIGHT + "版本编辑")
    @PostMapping("delVer")
    public Result delVer(Integer docRight, String appCode) {
        modelPermissionsService.delVer(docRight, appCode);
        return Result.success(true);
    }

    /**
     * 是否使用
     */
    @ApiOperation("是否使用")
    @OperationLog(LogsConstants.DOC_RIGHT + "是否使用")
    @PostMapping("isUse")
    public Result isUse(@RequestBody AddVerVO addVerVo) {
        addVerVo.setUpdateUser(getToken().getUsername());
        modelPermissionsService.isUse(addVerVo);
        return Result.success(true);
    }

    /**
     * 业务资料权限信息
     */
    @ApiOperation("业务资料权限信息")
    @OperationLog(LogsConstants.DOC_RIGHT + "业务资料权限信息")
    @PostMapping("detailsVer")
    public Result detailsVer(Long id) {
        return Result.success(modelPermissionsService.detailsVer(id));
    }

    /**
     * 获取指定版本对应的文件类型
     */
    @ApiOperation("业务资料权限信息")
    @OperationLog(LogsConstants.DOC_RIGHT + "业务资料权限信息")
    @PostMapping("getDocTreeByVerAndAppCode")
    public Result getDocTreeByVerAndAppCode(String appCode, Integer rightVer) {
        return Result.success(modelInformationService.getDocTreeByVerAndAppCode(appCode, rightVer));
    }

    /**
     * 获取角色列表
     */
    @ApiOperation("获取角色列表")
    @OperationLog(LogsConstants.DOC_RIGHT + "获取角色列表")
    @PostMapping("getRoleList")
    public Result getRoleList(@RequestBody EcmGetRoleListDTO ecmGetRoleListDTO) {
        SysRoleDTO sysRoleDTO = new SysRoleDTO();
        sysRoleDTO.setSystemCode(RoleConstants.ICMS.toString());
        //pageSize赋值为0，可查全表，不进行分页
        sysRoleDTO.setPageSize(IcmsConstants.ZERO);
        return Result.success(modelPermissionsService.getRoleList(sysRoleDTO,
                ecmGetRoleListDTO.getAppCode(), ecmGetRoleListDTO.getRightVer()));
    }

    /**
     * 获取选择角色的资料权限列表
     */
    @ApiOperation("获取选择角色的资料权限列表")
    @OperationLog(LogsConstants.DOC_RIGHT + "获取选择角色的资料权限列表")
    @PostMapping("getRoleDocRightList")
    public Result getRoleDocRightList(@RequestBody DocRightVO docRightVo) {
        docRightVo.setCurrentUser(getToken().getUsername());
        return Result.success(modelPermissionsService.getRoleDocRightList(docRightVo));
    }

    /**
     * 保存业务多维度资料权限配置
     */
    @ApiOperation("保存业务资料权限配置")
    @OperationLog(LogsConstants.DOC_RIGHT + "保存业务资料权限配置")
    @PostMapping("saveRoleAndLotDimDocRight")
    public Result saveRoleAndLotDimDocRight(@RequestBody DocRightRoleAndLotVO docRightVo) {
        modelPermissionsService.saveRoleAndLotDimDocRight(docRightVo, getToken());
        return Result.success(true);
    }

    /**
     * 获取维度列表
     */
    @ApiOperation("获取维度列表")
    @OperationLog(LogsConstants.DOC_RIGHT + "获取维度列表")
    @PostMapping("getDimensionList")
    public Result getDimensionList() {
        return Result.success(modelPermissionsService.getDimensionList());
    }

    /**
     * 新增业务维度
     */
    @ApiOperation("新增业务维度")
    @OperationLog(LogsConstants.DOC_RIGHT + "新增业务维度")
    @PostMapping("addDimension")
    public Result addDimension(@RequestBody EcmDimensionDef ecmDimensionDef) {
        ecmDimensionDef.setCreateUser(getToken().getUsername());
        modelPermissionsService.addDimension(ecmDimensionDef);
        return Result.success(true);
    }

    /**
     * 编辑业务维度
     */
    @ApiOperation("编辑业务维度")
    @OperationLog(LogsConstants.DOC_RIGHT + "编辑业务维度")
    @PostMapping("editDimension")
    public Result editDimension(@RequestBody EcmDimensionDef ecmDimensionDef) {
        ecmDimensionDef.setUpdateUser(getToken().getUsername());
        modelPermissionsService.editDimension(ecmDimensionDef);
        return Result.success(true);
    }

    /**
     * 删除业务维度
     */
    @ApiOperation("删除业务维度")
    @OperationLog(LogsConstants.DOC_RIGHT + "删除业务维度")
    @PostMapping("delDimension")
    public Result delDimension(Long id) {
        modelPermissionsService.delDimension(id);
        return Result.success(true);
    }

    /**
     * 批量删除业务维度
     */
    @ApiOperation("批量删除业务维度")
    @OperationLog(LogsConstants.DOC_RIGHT + "批量删除业务维度")
    @PostMapping("delLotDimension")
    public Result delLotDimension(Long[] ids) {
        modelPermissionsService.delLotDimension(ids);
        return Result.success(true);
    }

    /**
     * 关联维度
     */
    @ApiOperation("关联维度")
    @OperationLog(LogsConstants.DOC_RIGHT + "关联维度")
    @PostMapping("relateDimToApp")
    public Result relateDimToApp(@RequestBody DocRightVO docRightVo) {
        docRightVo.setCurrentUser(getToken().getUsername());
        return Result.success(modelPermissionsService.relateDimToApp(docRightVo));
    }

    /**
     * 获取关联维度列表
     */
    @ApiOperation("获取关联维度列表")
    @OperationLog(LogsConstants.DOC_RIGHT + "获取关联维度列表")
    @PostMapping("getRelateDimList")
    public Result getRelateDimList(@RequestBody DocRightVO docRightVo) {
        return Result.success(modelPermissionsService.getRelateDimList(docRightVo));
    }

    /**
     * 获取业务多维度资料权限配置列表
     */
    @ApiOperation("获取业务多维度资料权限配置列表")
    @OperationLog(LogsConstants.DOC_RIGHT + "获取业务多维度资料权限配置列表")
    @PostMapping("getLotDimDocRightList")
    public Result getLotDimDocRightList(Long id) {
        String userName = getToken().getUsername();
        return Result.success(modelPermissionsService.getLotDimDocRightList(id, userName));
    }

}
