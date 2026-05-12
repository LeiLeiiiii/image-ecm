package com.sunyard.ecm.controller.api.mobile;

import com.github.pagehelper.PageInfo;
import com.sunyard.ecm.annotation.LogManageAnnotation;
import com.sunyard.ecm.constant.IcmsConstants;
import com.sunyard.ecm.constant.LogsConstants;
import com.sunyard.ecm.constant.RoleConstants;
import com.sunyard.ecm.controller.BaseController;
import com.sunyard.ecm.dto.EcmRootDataDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiInfoPhoneDTO;
import com.sunyard.ecm.dto.ecm.EcmBusiStructureTreeDTO;
import com.sunyard.ecm.dto.ecm.EcmFileInfoDTO;
import com.sunyard.ecm.dto.mobile.EcmMobileBusiListDTO;
import com.sunyard.ecm.dto.mobile.EcmMobileCaptureBusiDTO;
import com.sunyard.ecm.dto.mobile.EcmMobileCaptureDocDTO;
import com.sunyard.ecm.dto.mobile.EcmMobileCaptureFileDTO;
import com.sunyard.ecm.dto.redis.EcmBusiDocRedisDTO;
import com.sunyard.ecm.dto.redis.EcmBusiInfoRedisDTO;
import com.sunyard.ecm.manager.DocMarkService;
import com.sunyard.ecm.manager.MobileCaptureService;
import com.sunyard.ecm.manager.OpenApiService;
import com.sunyard.ecm.service.OperateCaptureService;
import com.sunyard.ecm.manager.CaptureSubmitService;
import com.sunyard.ecm.manager.FileInfoService;
import com.sunyard.ecm.vo.EcmDocMarkVO;
import com.sunyard.ecm.vo.EcmSubmitVO;
import com.sunyard.ecm.vo.EcmsCaptureVO;
import com.sunyard.ecm.vo.FileInfoVO;
import com.sunyard.ecm.vo.MoveFileVO;
import com.sunyard.ecm.vo.RotateFileVO;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.log.annotation.ApiLog;
import com.sunyard.framework.log.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @author lw
 * @since: 2023/8/14
 * @Desc: 对外接口-移动端影像采集
 */
@Api(tags = "对外接口-移动端影像采集")
@RestController
@RequestMapping("api/mobile/capture")
public class ApiMobileCaptureController extends BaseController {

    @Resource
    private MobileCaptureService mobileCaptureService;
    @Resource
    private FileInfoService fileInfoService;
    @Resource
    private CaptureSubmitService captureSubmitService;
    @Resource
    private OpenApiService openApiService;
    @Resource
    private OperateCaptureService operateCaptureService;
    @Resource
    private DocMarkService docMarkService;
    
    /**
     * 获取分片上传大小
     */
    //策略管理配置查询接口
    @ApiOperation("策略管理配置查询接口")
    @OperationLog(LogsConstants.SYSTEM + "策略管理配置查询接口")
    @PostMapping("getUploadChunkSize")
    public Result getUploadChunkSize() {
        return operateCaptureService.getUploadChunkSize();
    }

    /**
     * 移动端影像扫描或修改
     */
    @ApiOperation("移动端影像扫描或修改")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "移动端影像扫描或修改")
    @LogManageAnnotation("采集业务（移动端）")
    @PostMapping(value = "scanOrUpdateEcmMobile", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Result scanOrUpdateEcmMobile(@RequestBody EcmRootDataDTO vo) {
        return Result.success(openApiService.businessDataServiceMobile(vo));
    }

    /**
     * 扫码获取移动端页面路径
     */
    @ApiOperation("扫码获取移动端页面路径")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "获取移动端页面路径")
    @PostMapping("getMobilePagePath")
    public Result getMobilePagePath(@RequestBody List<Long> busiIdList){
        return mobileCaptureService.getMobilePagePath(busiIdList,getToken());
    }

    /**
     * 根据采集页面扫描的key获取对应业务列表信息
     * @param ecmMobileCaptureBusiDTO 业务ID数组、业务主索引
     */
    @ApiOperation("获取对应的业务类型业务主索引列表")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "获取对应的业务类型业务主索引列表")
    @PostMapping("getBusinessList")
    public Result getBusinessList(@RequestBody EcmMobileBusiListDTO ecmMobileCaptureBusiDTO) {
        List<EcmBusiInfoPhoneDTO> busiInfoPhoneDTOS = mobileCaptureService.getMobileBusiList(ecmMobileCaptureBusiDTO);
        return Result.success(busiInfoPhoneDTOS);
    }

    /**
     * 模糊搜索对应的业务类型业务主索引列表
     * @param ecmMobileCaptureBusiDTO 业务ID数组、业务主索引
     */
    @ApiOperation("模糊搜索对应的业务类型业务主索引列表")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "获取对应的业务类型业务主索引列表")
    @PostMapping("getBusiListByBusiNo")
    public Result getBusiListByBusiNo(@RequestBody EcmMobileCaptureBusiDTO ecmMobileCaptureBusiDTO) {
        List<EcmBusiInfoPhoneDTO> busiInfoPhoneDTOS = mobileCaptureService.getBusinessList(ecmMobileCaptureBusiDTO);
        return Result.success(busiInfoPhoneDTOS);
    }
    
    @ApiOperation("获取最子级节点资料列表")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "获取最子级节点资料列表")
    @PostMapping("getInformation")
    public Result getInformation(@RequestBody EcmMobileCaptureDocDTO ecmMobileCaptureDocDTO){
        List<EcmBusiStructureTreeDTO> information = mobileCaptureService.getInformation(getToken(),ecmMobileCaptureDocDTO.getBusiId(),ecmMobileCaptureDocDTO.getInfoTypeName());
        return Result.success(information);
    }
    
    @ApiOperation("获取资料文件列表")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "获取资料文件列表")
    @PostMapping("getDocFileList")
    public Result getDocFileList(@RequestBody EcmMobileCaptureFileDTO ecmMobileCaptureFileDTO){
        PageInfo docFileList = mobileCaptureService.getDocFileList(ecmMobileCaptureFileDTO.getBusiId(), ecmMobileCaptureFileDTO.getDocId(),ecmMobileCaptureFileDTO.getShowAll(), ecmMobileCaptureFileDTO.getMarkDocId(), getToken());
        return Result.success(docFileList);
    }

    /**
     * 新增文件信息
     */
    @ApiOperation("新增文件信息")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "新增文件信息")
    @PostMapping("insertFileInfo")
    public Result insertFileInfoInner(@RequestBody EcmFileInfoDTO ecmFileInfoDTO){
        ecmFileInfoDTO.setFileSource(IcmsConstants.FILE_SOURCE_API_MOBILE);
        return fileInfoService.insertFileInfo(ecmFileInfoDTO, getToken());
    }

    /**
     * 图片编辑后插入文件信息
     */
    @ApiOperation("图片编辑后插入文件信息")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "图片编辑后插入文件信息")
    @PostMapping("rotateInsertFileInfo")
    public Result rotateInsertFileInfo(@RequestBody RotateFileVO vo) {
        return fileInfoService.rotateInsertFileInfo(vo, getToken());
    }

    /**
     * 替换文件前后插入文件信息
     */
    @ApiOperation("编辑文件前后插入文件信息")
    @OperationLog(LogsConstants.ICMS + "替换文件前后插入文件信息")
    @PostMapping("replaceInsertFileInfo")
    public Result replaceInsertFileInfo(@RequestBody RotateFileVO vo) {
        return fileInfoService.replaceInsertFileInfo(vo, getToken());
    }

    /**
     * 影像删除
     */
    @ApiOperation("影像删除")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "影像删除")
    @PostMapping("deleteFileInfo")
    public Result deleteFileInfo(@RequestBody FileInfoVO vo) {
        fileInfoService.deleteFileInfo(vo,getToken());
        return Result.success(true);
    }

    /**
     * 已删除列表中影像删除
     */
    @ApiOperation("已删除列表中影像删除")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "已删除列表中影像删除")
    @PostMapping("deleteFileInfoTrue")
    public Result deleteFileInfoTrue(@RequestBody FileInfoVO vo) {
        fileInfoService.deleteFileInfoTrue(vo,getToken());
        return Result.success(true);
    }

    /**
     * 文件移动
     */
    @ApiOperation("文件移动")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "文件移动")
    @PostMapping("moveFile")
    public Result moveFile(@RequestBody MoveFileVO moveFileVo) {
        return fileInfoService.moveFile(moveFileVo, getToken());
    }

    /**
     * 影像提交
     */
    @ApiOperation("影像提交")
    @ApiLog(LogsConstants.MOBILE_CAPTURE + "影像提交")
    @PostMapping("submit")
    public Result submit(@RequestBody EcmSubmitVO ecmSubmitVO) {
        AssertUtils.isNull(ecmSubmitVO.getBusiIdList(),"业务busiId不能为空");
        captureSubmitService.submit(getToken(),ecmSubmitVO);
        return Result.success(true);
    }

    /**
     * 影像文件归类
     */
    @ApiOperation("影像文件归类")
    @ApiLog(LogsConstants.CAPTURE + "影像文件归类")
    @PostMapping("classifyIcmsFile")
    public Result classifyIcmsFile(@RequestBody EcmsCaptureVO vo) {
        operateCaptureService.classifyIcmsFile(vo,getToken());
        return Result.success(true);
    }

    /**
     * 影像文件重命名
     * 无需添加文件历史记录，无还原操作
     * (改进) 重命名改为备注
     */
    @ApiOperation("影像文件备注")
    @ApiLog(LogsConstants.CAPTURE + "影像文件备注")
    @PostMapping("updateFileName")
    public Result updateFileName(@RequestBody FileInfoVO vo) {
        vo.setCurentUserId(getToken().getUsername());
        vo.setCurentUserName(getToken().getName());
        operateCaptureService.updateFileName(vo);
        return Result.success(true);
    }

    /**
     * 影像恢复
     */
    @ApiOperation("影像恢复")
    @ApiLog(LogsConstants.CAPTURE + "影像恢复")
    @PostMapping("restoreFileInfo")
    public Result restoreFileInfo(@RequestBody FileInfoVO vo) {
        fileInfoService.restoreFileInfo(vo,getToken());
        return Result.success(true);
    }

    /** --------------------------------------- 资料标记 --------------------------------------- */
    /**
     * 新增资料标记
     */
    @ApiOperation("新增资料标记")
    @ApiLog(LogsConstants.CAPTURE + "新增资料标记")
    @PostMapping("addDocMark")
    public Result addDocMark(@RequestBody EcmDocMarkVO vo) {
        return docMarkService.addDocMark(vo,getToken());
    }

    /**
     * 根据采集页面扫描的key获取对应业务列表信息
     * @param ecmMobileCaptureBusiDTO 业务ID数组、业务主索引
     */
    @ApiOperation("获取对应的业务类型业务主索引列表")
    @OperationLog(LogsConstants.MOBILE_CAPTURE + "获取对应的业务类型业务主索引列表")
    @PostMapping("getBusinessListAll")
    public Result getBusinessListAll(@RequestBody EcmMobileBusiListDTO ecmMobileCaptureBusiDTO) {
        List<EcmBusiInfoPhoneDTO> busiInfoPhoneDTOS = mobileCaptureService.getBusinessListAll(ecmMobileCaptureBusiDTO, getToken());
        return Result.success(busiInfoPhoneDTOS);
    }

    /**
     * 编辑资料标记
     */
    @ApiOperation("新增资料标记")
    @ApiLog(LogsConstants.CAPTURE + "新增资料标记")
    @PostMapping("editDocMark")
    public Result editDocMark(@RequestBody EcmDocMarkVO vo) {
        return docMarkService.editDocMark(vo,getToken());
    }

    /**
     * 删除资料标记
     */
    @ApiOperation("删除资料标记")
    @ApiLog(LogsConstants.CAPTURE + "删除资料标记")
    @PostMapping("deleteDocMark")
    public Result deleteDocMark(@RequestBody EcmDocMarkVO vo) {
        vo.setCurrentUserId(getToken().getUsername());
        docMarkService.deleteDocMark(vo, getToken());
        return Result.success(true);
    }

    /**
     * 查询资料标记
     */
    @ApiOperation("查询资料标记")
    @ApiLog(LogsConstants.CAPTURE + "查询资料标记")
    @PostMapping("searchDocMark")
    public Result searchDocMark(@RequestBody EcmDocMarkVO vo) {
        vo.setCurrentUserId(getToken().getUsername());
        List<EcmBusiDocRedisDTO> ecmBusiDocs = docMarkService.searchDocMark(vo, getToken());
        return Result.success(ecmBusiDocs);
    }

    /**
     * 获取影像采集按钮列表
     */
    @ApiOperation("获取影像采集按钮列表")
    @ApiLog(LogsConstants.CAPTURE + "获取影像采集按钮列表")
    @PostMapping("getRightButtonList")
    public Result getRightButtonList() {
        List<HashMap<String, String>> rightButtonList = mobileCaptureService.getRightButtonList(getToken().getRoleCodeList(), RoleConstants.IMAGE_CAPTURE);
        return Result.success(rightButtonList);
    }

    /**
     * 获取已删除界面钮列表
     */
    @ApiOperation("获取影像采集按钮列表")
    @ApiLog(LogsConstants.CAPTURE + "获取影像采集按钮列表")
    @PostMapping("getDeletedButtonList")
    public Result getDeletedButtonList() {
        List<HashMap<String, String>> rightButtonList = mobileCaptureService.getDeletedButtonList(getToken().getRoleCodeList(), RoleConstants.CAPTURE_VIEW_DELETED);
        return Result.success(rightButtonList);
    }

    /**
     * 编辑业务
     */
    @ApiOperation("编辑业务")
    @OperationLog(LogsConstants.CAPTURE + "编辑业务")
    @PostMapping("editBusi")
    public Result editBusi(@RequestBody EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        operateCaptureService.editBusi(ecmBusiInfoExtend, getToken());
        return Result.success();
    }

    /**
     * 获取业务类型的业务属性列表 返回前端展示直接识别类型
     */
    @ApiOperation("获取业务类型的业务属性列表")
    @OperationLog(LogsConstants.CAPTURE + "获取业务类型的业务属性列表")
    @PostMapping("getAppAttrList")
    public Result getAppAttrList(@RequestBody EcmBusiInfoRedisDTO ecmBusiInfoExtend) {
        return Result.success(operateCaptureService.getAppAttrList(ecmBusiInfoExtend.getAppCode(),
                ecmBusiInfoExtend.getBusiId(), null));
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
