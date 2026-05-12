package com.sunyard.module.storage.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;
import javax.validation.Valid;

import com.sunyard.module.storage.dto.S3Base64UploadDTO;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.log.annotation.OperationLog;
import com.sunyard.module.storage.constant.LogsPrefixConstants;
import com.sunyard.module.storage.dto.SplitUploadDTO;
import com.sunyard.module.storage.service.SplitUploadTaskService;
import com.sunyard.module.storage.vo.SplitUploadBigFileVo;
import com.sunyard.module.storage.vo.SplitUploadVO;
import com.sunyard.module.storage.vo.UploadSplitVO;

/**
 * 分片上传-分片任务记录(SysFile)表控制层
 *
 * @author zyl
 * @since 2022-08-22 17:47:31
 */
@RestController
@RequestMapping("storage/oss/splitUpload")
public class SplitUploadTaskController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.MENU_SPLIT_UPLOAD + "->";
    @Resource
    private SplitUploadTaskService splitUploadTaskService;

    /**
     * 获取上传进度
     * @param splitUploadBigFileVo splitUploadBigFileVo
     * @return Result
     */
    @OperationLog(BASELOG + "获取上传进度")
    @PostMapping("taskInfo")
    public Result<SplitUploadDTO> taskInfo(@RequestBody SplitUploadBigFileVo splitUploadBigFileVo) {
        return Result.success(splitUploadTaskService.getTaskInfo(
                splitUploadBigFileVo.getIdentifier(), splitUploadBigFileVo.getIsEncrypt(),
                splitUploadBigFileVo.getEquipmentId(), splitUploadBigFileVo.getId()));
    }

    /**
     * 获取上传信息（上传进度、预签名上传url、文件信息）
     * @param vo vo
     * @return Result
     */
    @OperationLog(BASELOG + "获取上传信息（上传进度、预签名上传url、文件信息）")
    @PostMapping("getUploadInfo")
    public Result<SplitUploadDTO> getUploadInfo(@RequestBody SplitUploadVO vo) {
        return Result.success(splitUploadTaskService.getUploadInfo(vo, getToken()));
    }

    /**
     * 创建一个上传任务
     * @param param param
     * @param bindingResult bindingResult
     * @return Result
     */
    @OperationLog(BASELOG + "创建一个上传任务")
    @PostMapping("initTask")
    public Result initTask(@Valid @RequestBody SplitUploadVO param, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage(),
                    ResultCode.PARAM_ERROR);
        }
        return Result.success(splitUploadTaskService.initTask(param, getToken().getId()));
    }

    /**
     * 获取每个分片的预签名上传地址
     * @param vo vo
     * @return Result
     */
    @OperationLog(BASELOG + "获取每个分片的预签名上传地址")
    @PostMapping("preSignUploadUrl")
    public Result preSignUploadUrl(@RequestBody SplitUploadBigFileVo vo) {
        return Result.success(splitUploadTaskService.genPreSignUploadUrl(vo.getIdentifier(),
                vo.getPartNumber(), vo.getType(), false, vo.getId(), vo.getEquipmentId()));
    }

    /**
     * 合并分片
     * @param vo vo
     * @return Result
     */
    @OperationLog(BASELOG + "合并分片")
    @PostMapping("merge")
    public Result merge(@RequestBody SplitUploadBigFileVo vo) {
        return Result.success(splitUploadTaskService.merge(vo.getIdentifier(), vo.getIsFlat(),
                vo.getId(), vo.getEquipmentId()));
    }

    /**
     * S3后端分片上传
     * @param file 文件对象
     * @param stEquipmentId 存储设备
     * @param fileSource 文件源
     * @param fileName 文件名
     * @param md5 md5
     * @param isEncrypt 是否加密
     * @return Result
     */
    @OperationLog(BASELOG + "S3后端分片上传")
    @PostMapping("useS3Upload")
    public Result useS3Upload(MultipartFile file, Long stEquipmentId, String fileSource,
                              String fileName, String md5, Integer isEncrypt, Boolean isFlat) {
        return Result.success(splitUploadTaskService.useS3Upload(file, getToken().getId(),
                stEquipmentId, fileSource, fileName, md5, isEncrypt, isFlat));
    }

    /**
     * S3后端Base64分片上传
     * @param data 数据
     * @return Result
     */
    @OperationLog(BASELOG + "S3后端Base64分片上传")
    @PostMapping("useS3UploadBase64")
    public Result useS3UploadBase64(@RequestBody S3Base64UploadDTO data) {
        return Result.success(splitUploadTaskService.useS3UploadBase64(data.getFile(), getToken().getId(),
                data.getStEquipmentId(), data.getFileSource(), data.getFileName(), data.getMd5(), data.getIsEncrypt(), data.getIsFlat()));
    }

    /**
     * 分片文件上传
     * @param uploadSplitVO uploadSplitVO
     * @return Result
     * @throws IOException 异常
     */
    @OperationLog(BASELOG + "分片上传")
    @PostMapping("uploadSplits")
    public Result uploadSplits(UploadSplitVO uploadSplitVO) throws IOException {
        uploadSplitVO.setInputStream(request.getInputStream());
        return Result.success(splitUploadTaskService.uploadSplit(uploadSplitVO));
    }

    /**
     * 分片文件上传
     * @param inputStream 输入流
     * @param partNumber 分片数量
     * @param equipmentId 存储设备
     * @param fileName 文件名
     * @param fileId 文件id
     * @param busiBatchNo 批次号
     * @param key key
     * @param uploadId 上传id
     * @param partSize 分片大小
     * @param isEncrypt 是否加密
     * @param identifier 标识
     * @return Result
     */
    @OperationLog(BASELOG + "分片上传")
    @PostMapping("uploadSplitOpen")
    public Result uploadSplitOpen(InputStream inputStream, Integer partNumber, Long equipmentId,
                                  String fileName, Long fileId, String busiBatchNo, String key,
                                  String uploadId, Long partSize, Integer isEncrypt,
                                  String identifier) {
        UploadSplitVO uploadSplitVO = new UploadSplitVO().setInputStream(inputStream)
                .setPartNumber(partNumber).setEquipmentId(equipmentId).setFileName(fileName)
                .setFileId(fileId).setBusiBatchNo(busiBatchNo).setKey(key).setUploadId(uploadId)
                .setPartSize(partSize).setIsEncrypt(isEncrypt).setIdentifier(identifier);
        return Result.success(splitUploadTaskService.uploadSplit(uploadSplitVO));
    }

}
