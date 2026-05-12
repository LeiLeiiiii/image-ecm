package com.sunyard.ecm.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.dto.ecm.EcmGetRoleListDTO;
import com.sunyard.module.system.api.dto.SysRoleDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.dto.ecm.EcmAddAppParamsDTO;
import com.sunyard.ecm.dto.ecm.EcmAddAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmAppAttrDTO;
import com.sunyard.ecm.dto.ecm.EcmAppDefDTO;
import com.sunyard.ecm.dto.ecm.EcmDocTreeDTO;
import com.sunyard.ecm.dto.ecm.SysStrategyDTO;
import com.sunyard.ecm.po.EcmAppAttr;
import com.sunyard.ecm.service.ModelBusiService;
import com.sunyard.ecm.service.ModelInformationService;
import com.sunyard.ecm.vo.DeleteBusiAttrVO;
import com.sunyard.ecm.vo.EcmAppDefAttrVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author： zyl
 * @create： 2023/4/12 17:41
 * @Desc 建模管理-业务建模
 */
@Api(tags = "建模管理-业务建模")
@RestController
@RequestMapping("model/busi")
public class ModelBusiController extends BaseController {
    @Resource
    private ModelBusiService modelBusiService;
    @Resource
    private ModelInformationService modelInformationService;

    /**
     * 新增业务类型
     */
    @ApiOperation("新增业务类型")
    @OperationLog("新增业务类型")
    @PostMapping("addBusiType")
    public Result addBusiType(@RequestBody EcmAppDefAttrVO ecmAppDefAttrVo) {
        return modelBusiService.addBusiType(ecmAppDefAttrVo, getToken().getUsername());
    }

    /**
     * 编辑业务类型
     */
    @ApiOperation("编辑业务类型")
    @OperationLog("新增业务类型")
    @PostMapping("editBusiType")
    public Result editBusiType(@RequestBody EcmAppDefAttrVO ecmAppDefAttrVo) {
        return modelBusiService.editBusiType(ecmAppDefAttrVo, getToken().getUsername());
    }

    /**
     * 删除业务类型
     */
    @ApiOperation("删除业务类型")
    @OperationLog("删除业务类型")
    @ApiImplicitParams({ @ApiImplicitParam(name = "appCode", value = "业务类型id", required = true), })
    @PostMapping("deleteBusiType")
    public Result deleteBusiType(@RequestBody EcmAppDefDTO ecmAppDefDTO) {
        return modelBusiService.deleteBusiType(ecmAppDefDTO);
    }

    /**
     * 查询业务类型树
     */
    @ApiOperation("查询业务类型树")
    @OperationLog("查询业务类型树")
    @PostMapping("searchBusiTypeTree")
    public Result<List<EcmAppDefAttrVO>> searchBusiTypeTree(String appCode) {
        return Result.success(modelBusiService.searchBusiTypeTree(appCode, getToken()));
    }

    /**
     * 查询业务类型树
     */
    @ApiOperation("查询业务类型树")
    @OperationLog("查询业务类型树")
    @PostMapping("searchBusiTypeTreeByHaveChild")
    public Result<List<EcmAppDefAttrVO>> searchBusiTypeTreeByHaveChild(String appCode) {
        return Result.success(modelBusiService.searchBusiTypeTreeByHaveChild(appCode, getToken()));
    }
    /**
     * 查询业务类型树
     */
    @ApiOperation("查询父级目录业务类型树")
    @OperationLog("查询父级目录业务类型树")
    @PostMapping("searchBusiTypeParentTree")
    public Result<List<EcmAppDefAttrVO>> searchBusiTypeParentTree(String appCode) {
        return Result.success(modelBusiService.searchBusiTypeParentTree(appCode, getToken()));
    }

    /**
     * 查询单个业务类型信息(属性和类型)
     */
    @ApiOperation("查询单个业务类型信息(属性和类型)")
    @OperationLog("查询单个业务类型信息(属性和类型)")
    @ApiImplicitParams({ @ApiImplicitParam(name = "appCode", value = "业务类型id", required = true),
            @ApiImplicitParam(name = "parentId", value = "父业务类型id", required = true),
            @ApiImplicitParam(name = "parentName", value = "父业务类型名称", required = true), })
    @PostMapping("searchBusiTypeInfo")
    public Result<EcmAppDefAttrVO> searchBusiTypeInfo(String appCode, String parentId,
                                                      String parentName) {
        return Result.success(modelBusiService.searchBusiTypeInfo(appCode, parentId, parentName));
    }

    /**
     * 新增业务属性
     */
    @ApiOperation("新增业务属性")
    @OperationLog("新增业务属性")
    @PostMapping("addBusiAttr")
    public Result<EcmAppAttr> addBusiAttr(@RequestBody EcmAppAttr ecmAppAttr) {
        return modelBusiService.addBusiAttr(ecmAppAttr, getToken());
    }

    /**
     * 编辑业务属性
     */
    @ApiOperation("编辑业务属性")
    @OperationLog("编辑业务属性")
    @PostMapping("editBusiAttr")
    public Result editBusiAttr(@RequestBody EcmAppAttr ecmAppAttr) {
        return modelBusiService.editBusiAttr(ecmAppAttr, getToken());
    }

    /**
     * 删除业务属性
     */
    @ApiOperation("删除业务属性")
    @OperationLog("删除业务属性")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appAttrId", value = "业务属性id集合", required = true), })
    @PostMapping("deleteBusiAttr")
    public Result deleteBusiAttr(@RequestBody DeleteBusiAttrVO deleteBusiAttrVO) {
        return modelBusiService.deleteBusiAttr(deleteBusiAttrVO, getToken());
    }

    /**
     * 查看单个业务属性详情
     */
    @ApiOperation("查看单个业务属性详情")
    @OperationLog("查看单个业务属性详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appAttrId", value = "业务属性id集合", required = true), })
    @PostMapping("searchOneBusiAttr")
    public Result<EcmAppAttr> searchOneBusiAttr(Long appAttrId) {
        return Result.success(modelBusiService.searchOneBusiAttr(appAttrId));
    }

    /**
     * 查询业务类型下的属性列表
     */
    @ApiOperation("查询业务类型下的属性列表")
    @OperationLog("查询业务类型下的属性列表")
    @ApiImplicitParams({ @ApiImplicitParam(name = "appCode", value = "业务类型id", required = true), })
    @PostMapping("searchBusiTypeAttrList")
    public Result<PageInfo<EcmAppAttrDTO>> searchBusiTypeAttrList(PageForm pageForm,
                                                                  String appCode) {
        return Result.success(modelBusiService.searchBusiTypeAttrList(pageForm, appCode));
    }

    /**
     * 复用-业务类型树展示
     */
    @ApiOperation("复用-业务类型树展示")
    @OperationLog("复用-业务类型树展示")
    @PostMapping("multiplexBusiTypeTree")
    public Result<List<EcmAppDefAttrVO>> multiplexBusiTypeTree(String appCode) {
        return Result.success(modelBusiService.searchBusiTypeTree(appCode, getToken()));
    }

    /**
     * 复用-业务属性列表展示(与业务类型中所选内容联动)
     */
    @ApiOperation("复用-业务属性列表展示(与业务类型中所选内容联动)")
    @OperationLog("复用-业务属性树展示(与业务类型中所选内容联动)")
    @ApiImplicitParams({ @ApiImplicitParam(name = "appCode", value = "业务类型id", required = true), })
    @PostMapping("multiplexBusiAttrAllList")
    public Result<List<EcmAppAttrDTO>> multiplexBusiAttrAllList(String appCode) {
        return Result.success(modelBusiService.multiplexBusiAttrAllList(appCode));
    }

    /**
     * 复用-新增业务属性
     */
    @ApiOperation("复用-新增业务属性")
    @OperationLog("复用-新增业务属性")
    @PostMapping("multiplexAddBusiAttr")
    public Result multiplexAddBusiAttr(@RequestBody EcmAddAttrDTO ecmAddAttrDTO) {
        return modelBusiService.multiplexAddBusiAttr(ecmAddAttrDTO, getToken());
    }

    /**
     * 查询资料树
     */
    @ApiOperation("查询资料树")
    @OperationLog("查询资料树")
    @PostMapping("searchInformationTree")
    public Result searchInformationTree(String docCode) {
        return modelInformationService.searchInformationTypeTree(docCode);
    }

    /**
     * 查询没有联资料树和已关联资料树
     */
    @ApiOperation("查询没有联资料树")
    @OperationLog("查询没有联资料树")
    @PostMapping("searchNoRelevanceInformationAll")
    public Result<Map> searchNoRelevanceInformationAll(String appCode) {
        return Result.success(modelBusiService.searchNoRelevanceInformationAll(appCode));
    }

    /**
     * 查询已关联资料
     */
    @ApiOperation("查询已关联资料")
    @OperationLog("查询已关联资料")
    @PostMapping("searchOldRelevanceInformation")
    public Result<List<EcmDocTreeDTO>> searchOldRelevanceInformation(String appCode) {
        return Result.success(modelBusiService.searchOldRelevanceInformation(appCode));
    }

    @ApiOperation("新增业务获取设备队列配置信息")
    @OperationLog("新增业务获取设备队列配置信息")
    @PostMapping("getAppGlobalParams")
    public Result<EcmAddAppParamsDTO> getAppGlobalParams() {
        return modelBusiService.getAppParams();
    }

    @ApiOperation("获取全局压缩参数配置")
    @OperationLog("获取全局压缩参数配置")
    @PostMapping("getAppZipParams")
    public Result<SysStrategyDTO> getAppZipParams() {
        return modelBusiService.getAppZipParams();
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
        return Result.success(modelBusiService.getRoleList(sysRoleDTO,
                ecmGetRoleListDTO.getAppCode()));
    }
}
