package com.sunyard.module.storage.controller.api;

import java.io.ByteArrayInputStream;
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
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.log.annotation.ApiLog;
import com.sunyard.module.storage.constant.LogsPrefixConstants;
import com.sunyard.module.storage.controller.BaseController;
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
@RequestMapping("api/storage/oss/splitUpload")
public class SplitUploadTaskOpenController extends BaseController {
    private static final String BASELOG = LogsPrefixConstants.API_SPLIT_UPLOAD + "->";
    @Resource
    private SplitUploadTaskService splitUploadTaskService;

    /**
     * 获取上传进度
     *
     * @param identifier 文件md5
     * @return Result
     */
    @ApiLog(BASELOG + "获取上传进度")
    @PostMapping("taskInfo")
    public Result<SplitUploadDTO> taskInfo(@RequestBody SplitUploadBigFileVo identifier) {
        AssertUtils.isNull(identifier.getIdentifier(), "identifier: 文件唯一标识(MD5)不能为空");
        return Result.success(splitUploadTaskService.getTaskInfo(identifier.getIdentifier(),
                identifier.getIsEncrypt(), identifier.getEquipmentId(), identifier.getId()));
    }

    /**
     * 获取上传信息（上传进度、预签名上传url、文件信息）
     *
     * @param vo vo
     * @return Result
     */
    @ApiLog(BASELOG + "获取上传信息（上传进度、预签名上传url、文件信息）")
    @PostMapping("getUploadInfo")
    public Result<SplitUploadDTO> getUploadInfo(@RequestBody SplitUploadVO vo) {
        vo.setIsOpen(true);
        return Result.success(splitUploadTaskService.getUploadInfo(vo, getToken()));
    }

    /**
     * 创建一个上传任务
     *
     * @param param         param
     * @param bindingResult bindingResult
     * @return Result
     */
    @ApiLog(BASELOG + "创建一个上传任务")
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
     *
     * @param vo vo
     * @return Result
     */
    @ApiLog(BASELOG + "获取每个分片的预签名上传地址")
    @PostMapping("preSignUploadUrl")
    public Result preSignUploadUrl(@RequestBody SplitUploadBigFileVo vo) {
        return Result.success(splitUploadTaskService.genPreSignUploadUrl(vo.getIdentifier(),
                vo.getPartNumber(), vo.getType(), true, vo.getId(), vo.getEquipmentId()));
    }

    /**
     * 合并分片
     *
     * @param vo vo
     * @return Result
     */
    @ApiLog(BASELOG + "合并分片")
    @PostMapping("merge")
    public Result merge(@RequestBody SplitUploadBigFileVo vo) {
        AssertUtils.isNull(vo.getIdentifier(), "identifier: 文件唯一标识(MD5)不能为空");
        AssertUtils.isNull(vo.getIsFlat(), "isFlat: isFlat不能为空");
        return Result.success(splitUploadTaskService.merge(vo.getIdentifier(), vo.getIsFlat(),
                vo.getId(), vo.getEquipmentId()));
    }

    /**
     * S3后端分片上传
     *
     * @param file          文件对象
     * @param stEquipmentId 设备id
     * @param userId        用户id
     * @param fileSource    文件源
     * @param fileName      文件名字
     * @param md5           md5
     * @param isEncrypt     是否加密
     * @return Result
     */
    @ApiLog(BASELOG + "S3后端分片上传")
    @PostMapping("useS3Upload")
    public Result useS3Upload(MultipartFile file, Long stEquipmentId, Long userId,
                              String fileSource, String fileName, String md5, Integer isEncrypt,
                              Boolean isFlat) {
        return Result.success(splitUploadTaskService.useS3Upload(file, userId, stEquipmentId,
                fileSource, fileName, md5, isEncrypt, isFlat));
    }

    /**
     * S3后端Base64分片上传
     * @param data 数据
     * @return Result
     */
    @ApiLog(BASELOG + "S3后端Base64分片上传")
    @PostMapping("useS3UploadBase64")
    public Result useS3UploadBase64(@RequestBody S3Base64UploadDTO data) {
        return Result.success(splitUploadTaskService.useS3UploadBase64(data.getFile(), getToken().getId(),
                data.getStEquipmentId(), data.getFileSource(), data.getFileName(), data.getMd5(), data.getIsEncrypt(), data.getIsFlat()));

    }

    /**
     * 分片文件上传
     *
     * @param uploadSplitVO 分片文件对象，用来是否是相同文件
     * @return Result
     */
    @ApiLog(BASELOG + "分片上传")
    @PostMapping("uploadSplits")
    public Result uploadSplits(@RequestBody UploadSplitVO uploadSplitVO) {
        uploadSplitVO.setInputStream(new ByteArrayInputStream(uploadSplitVO.getBytes()));
        return Result.success(splitUploadTaskService.uploadSplit(uploadSplitVO));
    }

    /**
     * 分片文件上传
     *
     * @param inputStream 输入流
     * @param partNumber  分片数量
     * @param equipmentId 设备id
     * @param fileName    文件名
     * @param fileId      文件id
     * @param busiBatchNo 业务批次号
     * @param key         key
     * @param uploadId    上传id
     * @param partSize    分片大小
     * @return Result
     */
    @ApiLog(BASELOG + "分片上传")
    @PostMapping("uploadSplitOpen")
    public Result uploadSplitOpen(InputStream inputStream, Integer partNumber, Long equipmentId,
                                  String fileName, Long fileId, String busiBatchNo, String key,
                                  String uploadId, Long partSize) {
        UploadSplitVO uploadSplitVO = new UploadSplitVO().setInputStream(inputStream)
                .setPartNumber(partNumber).setEquipmentId(equipmentId).setFileName(fileName)
                .setFileId(fileId).setBusiBatchNo(busiBatchNo).setKey(key).setUploadId(uploadId)
                .setPartSize(partSize);
        return Result.success(splitUploadTaskService.uploadSplit(uploadSplitVO));
    }

}
