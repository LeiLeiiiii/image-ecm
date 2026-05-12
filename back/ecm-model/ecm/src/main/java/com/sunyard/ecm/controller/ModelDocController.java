package com.sunyard.ecm.controller;

import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.dto.ecm.EcmDtdAttrInfoDTO;
import com.sunyard.ecm.dto.ecm.EcmDtdAttrMulDTO;
import com.sunyard.ecm.dto.ecm.EcmMoveDtdAttrDTO;
import com.sunyard.ecm.po.EcmDtdAttr;
import com.sunyard.ecm.po.EcmDtdDef;
import com.sunyard.ecm.service.ModelDocService;
import com.sunyard.ecm.vo.DeleteDocumentAttrVO;
import com.sunyard.ecm.vo.EcmDtdDefVO;
import com.sunyard.framework.common.page.PageForm;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.log.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author： zyl
 * @create： 2023/4/20 9:12
 * @Desc: 建模管理-单证建模
 */
@Api(tags = "建模管理-单证建模")
@RestController
@RequestMapping("model/doc")
public class ModelDocController extends BaseController {
    @Resource
    private ModelDocService modelDocService;

    /**
     * 新增单证类型
     */
    @ApiOperation("新增单证类型")
    @OperationLog("新增单证类型")
    @PostMapping("addDocumentType")
    public Result addDocumentType(@RequestBody EcmDtdDefVO ecmDtdDefVo) {
        return modelDocService.addDocumentType(ecmDtdDefVo, getToken().getUsername());
    }

    /**
     * 编辑单证类型管理
     */
    @ApiOperation("编辑单证类型管理")
    @OperationLog("编辑单证类型管理")
    @PostMapping("editDocumentType")
    public Result editDocumentType(@RequestBody EcmDtdDefVO ecmDtdDefVo) {
        return modelDocService.editDocumentType(ecmDtdDefVo, getToken().getUsername());
    }

    /**
     * 删除单证类型管理
     */
    @ApiOperation("删除单证类型管理")
    @OperationLog("删除单证类型管理")
    @PostMapping("deleteDocumentType")
    public Result deleteDocumentType(Long dtdTypeId, String dtdName) {
        return modelDocService.deleteDocumentType(dtdTypeId, dtdName);
    }

    /**
     * 复用-新增单证属性
     */
    @ApiOperation("复用-新增单证属性")
    @OperationLog("复用-新增单证属性")
    @PostMapping("multiplexAddDocumentAttr")
    public Result multiplexAddDocumentAttr(@RequestBody EcmDtdAttrMulDTO ecmAddAttrDTO) {
        return modelDocService.multiplexAddDocumentAttr(ecmAddAttrDTO, getToken());
    }

    /**
     * 新增单证属性
     */
    @ApiOperation("新增单证属性")
    @OperationLog("新增单证属性")
    @PostMapping("addDocumentAttr")
    public Result addDocumentAttr(@RequestBody EcmDtdAttr ecmDtdAttr) {
        return modelDocService.addDocumentAttr(ecmDtdAttr, getToken().getUsername());
    }

    /**
     * 编辑单证属性
     */
    @ApiOperation("新增单证属性")
    @OperationLog("新增单证属性")
    @PostMapping("editDocumentAttr")
    public Result editDocumentAttr(@RequestBody EcmDtdAttr ecmDtdAttr) {
        return modelDocService.editDocumentAttr(ecmDtdAttr, getToken());
    }

    /**
     * 删除单证属性
     */
    @ApiOperation("删除单证属性")
    @OperationLog("删除单证属性")
    @PostMapping("deleteDocumentAttr")
    public Result deletDocumentAttr(@RequestBody DeleteDocumentAttrVO deleteDocumentAttrVO) {
        return modelDocService.deleteDocumentAttr(deleteDocumentAttrVO, getToken());
    }

    /**
     * 查看单证属性
     */
    @ApiOperation("查看单证属性")
    @OperationLog("查看单证属性")
    @PostMapping("searchDocumentAttr")
    public Result<EcmDtdAttr> searchDocumentAttr(Long dtdAttrId) {
        return modelDocService.searchDocumentAttr(dtdAttrId);
    }

    /**
     * 查看单证属性分页列表
     */
    @ApiOperation("查看单证属性分页列表")
    @OperationLog("查看单证属性分页列表")
    @PostMapping("searchDocumentAttrList")
    public Result<PageInfo<EcmDtdAttrInfoDTO>> searchDocumentAttrList(PageForm pageForm,
                                                                      Long dtdTypeId) {
        return modelDocService.searchDocumentAttrList(pageForm, dtdTypeId);
    }

    /**
     * 查看单证类型
     */
    @ApiOperation("查看文档类型(也可用户复用确定按钮的回显数据)")
    @OperationLog("查看文档类型(也可用户复用确定按钮的回显数据)")
    @PostMapping("searchDocumentType")
    public Result<EcmDtdDef> searchDocumentType() {
        return modelDocService.searchDocumentType();
    }

    /**
     * 查看单个文档类型详情
     */
    @ApiOperation("查看单个文档类型详情")
    @OperationLog("查看单个文档类型详情")
    @PostMapping("searchOneDocumentType")
    public Result<EcmDtdDefVO> searchOneDocumentType(Long dtdTypeId) {
        return modelDocService.searchOneDocumentType(dtdTypeId);
    }

    /**
     * 复用-文档类型列表
     */
    @ApiOperation("复用-文档类型列表")
    @OperationLog("复复用-文档类型列表")
    @PostMapping("multiplexDocumentTypeList")
    public Result<List<EcmDtdDef>> multiplexDocumentTypeList() {
        return modelDocService.searchDocumentType();
    }

    /**
     * 复用-文档属性列表
     */
    @ApiOperation("复用-文档属性列表")
    @OperationLog("复复用-文档属性列表")
    @PostMapping("multiplexDocumentArrtList")
    public Result<List<EcmDtdAttr>> multiplexDocumentArrtList(Long dtdTypeId) {
        return modelDocService.multiplexDocumentArrtList(dtdTypeId);
    }

    /**
     * 移动文档属性列表顺序
     */
    @ApiOperation("移动文档属性列表顺序")
    @OperationLog("移动文档属性列表顺序")
    @PostMapping("moveDocumentArrtList")
    public Result moveDocumentArrtList(@RequestBody EcmMoveDtdAttrDTO ecmMoveDtdAttrDTO) {
        return modelDocService.moveDocumentArrtList(ecmMoveDtdAttrDTO, getToken().getUsername());
    }

}
