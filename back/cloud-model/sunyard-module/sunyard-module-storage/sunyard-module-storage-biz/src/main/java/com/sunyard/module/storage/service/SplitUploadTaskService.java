package com.sunyard.module.storage.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import com.spire.pdf.PdfDocument;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.module.storage.util.SunCacheUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.annotation.Lock4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.FileUtils;
import com.sunyard.framework.common.util.encryption.Base64Utils;
import com.sunyard.framework.common.util.encryption.Md5Utils;
import com.sunyard.framework.img.util.ImgPythonCheckUtils;
import com.sunyard.framework.img.util.RectifyImageUtils;
import com.sunyard.framework.onlyoffice.tools.OnlyOfficeUtil;
import com.sunyard.framework.redis.constant.TimeOutConstants;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.framework.spire.util.SplitPdfUtils;
import com.sunyard.module.storage.config.factory.FileStorage;
import com.sunyard.module.storage.config.factory.impl.FileStorageService;
import com.sunyard.module.storage.config.properties.StorageOnlyOfficeProperties;
import com.sunyard.module.storage.config.properties.StorageUploadProperties;
import com.sunyard.module.storage.constant.FileConstants;
import com.sunyard.module.storage.constant.StateConstants;
import com.sunyard.module.storage.dto.EncryptDTO;
import com.sunyard.module.storage.dto.FileEncryptInfoDTO;
import com.sunyard.module.storage.dto.FilePartInfoDTO;
import com.sunyard.module.storage.dto.SplitUploadDTO;
import com.sunyard.module.storage.dto.SplitUploadRecordDTO;
import com.sunyard.module.storage.dto.StFileDTO;
import com.sunyard.module.storage.dto.SysFileDTO;
import com.sunyard.module.storage.dto.UploadDTO;
import com.sunyard.module.storage.manager.StFileService;
import com.sunyard.module.storage.mapper.StEquipmentMapper;
import com.sunyard.module.storage.mapper.StSplitUploadMapper;
import com.sunyard.module.storage.po.StEquipment;
import com.sunyard.module.storage.po.StFile;
import com.sunyard.module.storage.util.FileEncryptUtils;
import com.sunyard.module.storage.util.HashPathGeneratorUtils;
import com.sunyard.module.storage.util.WordTextExtractor;
import com.sunyard.module.storage.vo.FileSplitPdfVO;
import com.sunyard.module.storage.vo.SplitUploadVO;
import com.sunyard.module.storage.vo.UploadSplitVO;
import com.sunyard.module.system.api.ParamApi;
import com.sunyard.module.system.api.dto.SysParamDTO;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.Part;

/**
 * 分片上传
 * @author zyl
 * @Description
 * @since 2023/7/20 10:05
 */
@Slf4j
@Service
public class SplitUploadTaskService {
    private volatile static Map<String, Object> ST_FILE_MAP = new HashMap<>();
    // 锁获取时间超时为30秒
    private final Long acquireTimeout = 30 * 1000L;
    // 锁自动失效时间为10秒
    private final Long expire = 10 * 60 * 1000L;

    @Resource
    private StorageOnlyOfficeProperties storageOnlyOfficeProperties;
    @Resource
    private StorageUploadProperties storageUploadProperties;
    @Resource
    private LockTemplate lockTemplate;
    @Resource
    private StEquipmentMapper stEquipmentMapper;
    @Resource
    private StSplitUploadMapper stSplitUploadMapper;
    @Resource
    private CacheCommonService cacheCommonService;
    @Resource
    private StFileService stFileService;
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private EncryptService encryptService;
    @Resource
    private RedisUtils redisUtil;
    @Resource
    private ParamApi paramApi;



    /*** -------------------------------------------------- 分片上传(前端请求OSS服务进行上传) --------------------------------------------------*/
    /***/
    public StFileDTO getByIdentifier(String identifier, Long id) {
        StFile stFile = new StFile();
        stFile.setSourceFileMd5(identifier);
        stFile.setId(id);
        List<StFileDTO> stFiles = stFileService.selectFileDTOByPO(stFile);
        if (!CollectionUtils.isEmpty(stFiles)) {
            return stFiles.get(0);
        }
        return null;
    }

    /**
     * 初始化一个任务
     * @param param 分片参数
     * @return Result
     */
    @Lock4j(keys = { "#param.identifier" }, acquireTimeout = 6000)
    public SplitUploadDTO initTask(SplitUploadVO param, Long userId) {
        if (isSecondsUpload()) {
            StFileDTO byIdentifier = getByIdentifier(param.getIdentifier(), param.getId());
            if (!ObjectUtil.isNull(byIdentifier)) {
                throw new SunyardException(ResultCode.PARAM_ERROR, "本次上传有内容一样的文件，只能成功上传其中一个");
            }
        }
        StFileDTO task = new StFileDTO();
        String fileName = param.getFileName();
        String fileName1 = IdUtil.randomUUID();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        String key =getPathKey(fileName1,suffix);
        task.setObjectKey(key);
        //获取创建上传任务
        FileStorage fileStorage = fileStorageService
                .getFileStorage(String.valueOf(param.getEquipmentId()));
        task = fileStorage.initTask(task);
        int chunkNum = (int) Math.ceil(param.getTotalSize() * 1.0 / param.getChunkSize());
        //判断是否加密
        String value = null;
        if (StateConstants.IS_ENCRYPT.equals(param.getIsEncrypt())) {
            value = paramApi.searchValueByKey(StateConstants.FILE_ENCRYPT_TYPE).getData()
                    .getValue();
        }
        task.setChunkNum(chunkNum).setChunkSize(param.getChunkSize()).setSize(param.getTotalSize())
                .setFileMd5(param.getIdentifier()).setOriginalFilename(fileName)
                .setFilename(fileName1).setFileSource(param.getFileSource()).setExt(suffix)
                .setCreateUser(userId).setSourceFileMd5(param.getSourceFileMd5())
                .setEquipmentId(param.getEquipmentId())
                //设置是否加密、加密密钥、加密标识符
                .setIsEncrypt(param.getIsEncrypt())
                .setEncryptKey((StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value)
                        ? Base64Utils.encodeBase64(storageUploadProperties.getEncryptKey())
                        : null))
                .setEncryptType((StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? 0 : 1));

        stFileService.insert(task);
        synchronized (task.getId()) {
            //缓存文件信息
            ST_FILE_MAP.put(task.getId().toString(), task);
        }
        log.info("文件信息插入缓存：{}", ST_FILE_MAP);
        SplitUploadRecordDTO dto = new SplitUploadRecordDTO();
        BeanUtil.copyProperties(task, dto);
        return new SplitUploadDTO().setFinished(false).setTaskRecord(dto)
                .setPath(fileStorage.getPath(key));
    }

    /**
     * 获取文件地址
     * @param endpoint 域名
     * @param bucket 存储桶
     * @param objectKey 文件在存储桶的路径
     * @return Result
     */
    public String getPath(String endpoint, String bucket, String objectKey) {
        if (StrUtil.isBlank(endpoint)) {
            return StrUtil.format("{}{}/{}", endpoint, bucket, objectKey);
        }
        return StrUtil.format("{}/{}/{}", endpoint, bucket, objectKey);
    }

    /**
     * 获取上传进度
     * @param identifier 文件md5
     * @param isEncrypt 是否加密
     * @param equipmentId 存储设备id
     * @param id id
     * @return SplitUploadDTO
     */
    public SplitUploadDTO getTaskInfo(String identifier, Integer isEncrypt, Long equipmentId,
                                      Long id) {
        AssertUtils.isNull(identifier, "identifier不能为空");
        StFileDTO task = null;
        List<StFileDTO> stFileList = null;
        //根据md5获取文件
        stFileList = getByIdentifierAndId(identifier, id);
        if (CollectionUtil.isEmpty(stFileList)) {
            return null;
        } else {
            //如果不能拿到文件id那就从没有上传完成的集合中根据时间顺序去最新的时间的那一条去断点续传
            task = stFileList.get(0);
        }
        if (!isSecondsUpload()) {
            //不妙传
            if (ObjectUtil.isNotEmpty(id)) {
                //前端传任务管理器中的未上传成功的文件id
                List<StFileDTO> collect = stFileList.stream()
                        .filter(p -> Objects.equals(p.getId(), id)).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(collect)) {
                    task = collect.get(0);
                }
            } else {
                //不妙传 但是有没有上传成功的文件 继续上传未成功上传的文件
                List<StFileDTO> collect = stFileList.stream()
                        .filter(p -> FileConstants.FILE_NOT_FINISH.equals(p.getIsUploadOk()))
                        .collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(collect)) {
                    task = collect.get(0);
                }
            }
        }
        SplitUploadRecordDTO dto = new SplitUploadRecordDTO();
        BeanUtil.copyProperties(task, dto);
        //通过指定设备id，来确定存储路径/存储方式
        if (ObjectUtil.isEmpty(equipmentId)) {
            long l2 = System.currentTimeMillis();
            StEquipment stEquipment = stEquipmentMapper
                    .selectOne(new LambdaQueryWrapper<StEquipment>().eq(StEquipment::getId,
                            task.getEquipmentId()));
            long l3 = System.currentTimeMillis();
            log.info("获取存储设备信息耗时：" + (l3 - l2) + " ms");
            equipmentId = stEquipment.getId();
        }
        FileStorage fileStorage = fileStorageService.getFileStorage(String.valueOf(equipmentId));

        SplitUploadDTO result = new SplitUploadDTO().setFinished(true).setTaskRecord(dto)
                .setPath(fileStorage.getPath(task.getObjectKey()));
        if (task.getIsUploadOk() != null && 1 != task.getIsUploadOk()) {
            //没上传完成
            //获取已上传分片
            List<Part> partSummaryList = fileStorage.getTaskInfo(task);
            if (CollectionUtil.isEmpty(partSummaryList)) {
                result.setFinished(false);
                //有可能是已经上传完成
                if (!isSecondsUpload()) {
                    return null;
                }
            } else {
                result.setFinished(false).getTaskRecord().setExitPartList(partSummaryList);
            }
        } else {
            //已经上传完成
            if (!isSecondsUpload()) {
                //不妙传
                return null;
            }
        }
        return result;
    }

    /**
     * 获取上传信息（上传进度、预签名上传url、文件信息）
     * @param splitUploadVO 上传所需传值
     * @return Result
     */
    public SplitUploadDTO getUploadInfo(SplitUploadVO splitUploadVO, AccountToken token) {
        AssertUtils.isNull(splitUploadVO.getIsOpen(), "isOpen 不能为空");
        AssertUtils.isNull(splitUploadVO.getType(), "type 不能为空");
        AssertUtils.isNull(splitUploadVO.getEquipmentId(), "equipmentId 不能为空");
        AssertUtils.isNull(splitUploadVO.getBusiBatchNo(), "busiBatchNo 不能为空");
        AssertUtils.isNull(splitUploadVO.getFileName(), "fileName 不能为空");
        AssertUtils.isNull(splitUploadVO.getTotalSize(), "totalSize 不能为空");
        AssertUtils.isNull(splitUploadVO.getChunkSize(), "chunkSize 不能为空");
        AssertUtils.isNull(splitUploadVO.getSourceFileMd5(), "sourceFileMd5 不能为空");
        AssertUtils.isNull(splitUploadVO.getIsEncrypt(), "isEncrypt 不能为空");
        AssertUtils.isNull(splitUploadVO.getFileSource(), "文件来源 不能为空");
        //获取上传进度
        SplitUploadDTO splitUploadDTO = getTaskInfo(splitUploadVO.getIdentifier(),
                splitUploadVO.getIsEncrypt(), splitUploadVO.getEquipmentId(),
                splitUploadVO.getId());
        if (splitUploadDTO == null) {
            //没有上传过就创建一个新的上传任务
            SplitUploadVO param = new SplitUploadVO();
            param.setEquipmentId(splitUploadVO.getEquipmentId());
            param.setBusiBatchNo(splitUploadVO.getBusiBatchNo());
            param.setFileName(splitUploadVO.getFileName());
            param.setIdentifier(splitUploadVO.getIdentifier());
            param.setTotalSize(splitUploadVO.getTotalSize());
            param.setChunkSize(splitUploadVO.getChunkSize());
            param.setSourceFileMd5(splitUploadVO.getSourceFileMd5());
            param.setIsEncrypt(splitUploadVO.getIsEncrypt());
            param.setFileSource(splitUploadVO.getFileSource());
            splitUploadDTO = initTask(param, token.getId());
        }
        //上传过进行断点续传
        //得到已上传的分片记录
        SplitUploadRecordDTO taskRecord = splitUploadDTO.getTaskRecord();
        //已上传的分片信息
        List<Part> exitPartList = taskRecord.getExitPartList();
        List<Integer> partNumberList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(exitPartList)) {
            partNumberList = exitPartList.stream().map(Part::partNumber)
                    .collect(Collectors.toList());
        }
        //总分片数量
        Integer chunkNum = taskRecord.getChunkNum();
        //文件md5
        String identifier = taskRecord.getSourceFileMd5();
        //文件id
        Long id = taskRecord.getId();
        //所有分片的上传地址
        List<String> urlList = new ArrayList<>();
        for (Integer partNumber = 1; partNumber <= chunkNum; partNumber++) {
            if (partNumberList.contains(partNumber)) {
                // 该分片已经上传过
            } else {
                // 没上传过的分片进行上传
                // 1、获取上传路径
                String url = genPreSignUploadUrl(identifier, partNumber, splitUploadVO.getType(),
                        splitUploadVO.getIsOpen(), id, splitUploadVO.getEquipmentId());
                urlList.add(url);
            }
        }
        splitUploadDTO.setUrlList(urlList);
        //去除非必须信息
        splitUploadDTO.getTaskRecord().setBucketName(null);
        splitUploadDTO.getTaskRecord().setFilePath(null);
        splitUploadDTO.getTaskRecord().setObjectKey(null);
        splitUploadDTO.getTaskRecord().setUrl(null);
        return splitUploadDTO;
    }

    /**
     * 生成预签名上传url
     * @param identifier 文件md5
     * @param partNumber 分片位置
     * @param type 区分移动端和pc端，移动端传值为0，pc端不传
     * @param isOpen 是否是open
     * @param id id
     * @param equipmentId 存储设备id
     * @return Result
     */
    public String genPreSignUploadUrl(String identifier, Integer partNumber, String type,
                                      Boolean isOpen, Long id, Long equipmentId) {
        AssertUtils.isNull(identifier, "identifier: 文件唯一标识(MD5)不能为空");
        AssertUtils.isNull(partNumber, "partNumber: 上传某个分片标识不能为空");
        AssertUtils.isNull(type, "type不能为空");
        StFileDTO task = (StFileDTO) ST_FILE_MAP.get(id);
        if (ObjectUtil.isEmpty(task)) {
            task = getByIdentifier(identifier, id);
        }
        AssertUtils.isNull(task, "分片任务不存在");
        //测试存储设备连接
        String newIp = null;
        String newPort = null;
        //区分移动端和pc端，移动端传值为0，pc端不传
        if (FileConstants.PC_FILE_UPLOADURL.equals(type)) {
            newIp = storageUploadProperties.getLanProxyIp();
            newPort = storageUploadProperties.getLanProxyPort();
        } else {
            newIp = storageUploadProperties.getWanProxyIp();
            newPort = storageUploadProperties.getWanProxyPort();
        }
        //通过指定设备id，来确定存储路径/存储方式
        //通过指定设备id，来确定存储路径/存储方式
        if (ObjectUtil.isEmpty(equipmentId)) {
            StEquipment stEquipment = stEquipmentMapper
                    .selectOne(new LambdaQueryWrapper<StEquipment>().eq(StEquipment::getId,
                            task.getEquipmentId()));
            equipmentId = stEquipment.getId();
        }
        StringBuffer nasUrl = new StringBuffer();
        String newUrl = "";
        if (newIp.contains("http")) {
            newUrl = newIp;
        } else {
            newUrl = "http://" + newIp + ":" + newPort;
        }
        nasUrl.append(newUrl + "/" + "web-api/storage" + (isOpen ? "/api" : "")
                + "/storage/oss/splitUpload/uploadSplitOpen?");
        nasUrl.append("partNumber=" + partNumber);
        nasUrl.append("&equipmentId=" + task.getEquipmentId());
        nasUrl.append("&key=" + task.getObjectKey());
        nasUrl.append("&uploadId=" + task.getUploadId());
        //        nasUrl.append("&busiBatchNo=" + task.getBusiBatchNo());
        nasUrl.append("&fileId=" + task.getId());
        nasUrl.append("&fileName=" + task.getFilename() + "." + task.getExt());
        nasUrl.append("&identifier=" + task.getFileMd5());
        String taskUrl = task.getUrl();
        task.setUrl(nasUrl.toString());
        //        task.setConfigId(Long.valueOf(partNumber));
        String url = nasUrl.toString();
        task.setUrl(taskUrl);
        //        task.setConfigId(null);
        return url;
    }

    /**
     * 合并分片
     * @param identifier 文件md5
     * @param isFlat 是否偏离矫正
     * @param id id
     * @param equipmentId 存储设备id
     * @return Result
     */
    public SysFileDTO merge(String identifier, Boolean isFlat, Long id, Long equipmentId) {
        AssertUtils.isNull(isFlat, "isFlat：是否偏离矫正判断条件不能为空");
        long start = System.currentTimeMillis();
        StFileDTO task = getByIdentifier(identifier, id);
        AssertUtils.isNull(task, "分片任务不存");
        //通过指定设备id，来确定存储路径/存储方式
        if (ObjectUtil.isEmpty(equipmentId)) {
            StEquipment stEquipment = stEquipmentMapper.selectById(task.getEquipmentId());
            equipmentId = stEquipment.getId();
        }
        FileStorage fileStorage = fileStorageService.getFileStorage(String.valueOf(equipmentId));


        fileStorage.merge(task,lockTemplate);
        ST_FILE_MAP.remove(task.getId().toString());
        //偏离矫正
//        if (FileCheckUtils.isImage(fileStorage.getFileStream(task.getObjectKey())) && isFlat) {
//            log.info("进行了偏离矫正");
//            task = pictureCorrection(task, fileStorage);
//        }
        String suffix = task.getExt();
        //缓存
        SysFileDTO sysFileDTO = new SysFileDTO();

        //抽取文本
        Integer isEncrypt = task.getIsEncrypt();
        InputStream fileStream = fileStorage.getFileStream(task.getObjectKey());
        sysFileDTO.setIsFilePassword(isInputStreamPasswordProtected(fileStream,task.getOriginalFilename()));
        fileStream = fileStorage.getFileStream(task.getObjectKey());
        if(suffix.equals("doc") || suffix.equals("docx")){
            if (StateConstants.IS_ENCRYPT.equals(isEncrypt)) {
                if(fileStream==null){
                    fileStream = fileStorage.getFileStream(task.getObjectKey());
                }
                InputStream decrypt = FileEncryptUtils.decrypt(fileStream, task.getEncryptKey(), task.getEncryptType(), task.getEncryptLen() == null ? 0 : task.getEncryptLen().intValue());
                byte[] bytes = WordTextExtractor.copyInputStream(decrypt);
                String text = WordTextExtractor.extractTextHybrid(bytes);
                if (text != null && text.contains("EncryptionInfo")) {
                    sysFileDTO.setIsFilePassword(true);
                }
                sysFileDTO.setContentFirstPage(text);
                fileStream = new ByteArrayInputStream(bytes);
                isEncrypt = 0;
            }
        }

        if (storageUploadProperties.getCacheEnable()) {
            if(fileStream==null){
                fileStream = fileStorage.getFileStream(task.getObjectKey());
            }
            cacheCommonService.cacheFileAsync(fileStream, task.getUrl(), task.getId(),
                    task.getExt(), isEncrypt, task.getEncryptKey(), task.getEncryptType(),
                    task.getEncryptLen() == null ? 0 : task.getEncryptLen().intValue(),null);
        }

        task.setIsUploadOk(StateConstants.COMMON_ONE);
        stFileService.update(task);

        BeanUtils.copyProperties(task, sysFileDTO);
        //去除非必须信息
        sysFileDTO.setBucketName(null);
        sysFileDTO.setEquipmentId(null);
        sysFileDTO.setFilePath(null);
        sysFileDTO.setFileSource(null);
        sysFileDTO.setObjectKey(null);
        sysFileDTO.setUrl(null);
        long end = System.currentTimeMillis();
        log.info("合并文件总耗时（合并+合并后操作）：{}(毫秒)", end - start);
        return sysFileDTO;
    }

    /**
     * 加密文件
     * @param task 文件对象
     * @param fileStorage 文件存储对象
     * @return Result
     */
    public StFileDTO fileEncrypt(StFileDTO task, FileStorage fileStorage) {
        InputStream inputStreamFromUrl = null;
        inputStreamFromUrl = fileStorage.getFileStream(task.getObjectKey());
        AssertUtils.isNull(inputStreamFromUrl, "文件加密有误：获取源文件流出错");
        //调用加密方法
        FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStreamFromUrl,
                task.getEncryptKey(), task.getEncryptType());
        EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
        InputStream encryptStream = dto.getInputStream();
        stFileService.updateFileEncryptLength((long) dto.getLength(), task.getId());
        AssertUtils.isNull(encryptStream, "文件加密有误：加密文件流出错");
        String uuid = IdUtil.randomUUID();
        String key=getPathKey(uuid,task.getExt());
        try {
            UploadDTO uploadDTO = new UploadDTO().setInputStream(encryptStream).setKey(key)
                    .setFileSize((long) encryptStream.available()).setFilePath(task.getObjectKey())
                    .setChunkSize(storageUploadProperties.getChunkSize());
            log.info("将加密后的文件重新上传");
            long start = System.currentTimeMillis();
            StFileDTO stFile = fileStorage.replaceFile(uploadDTO);
            task.setUrl(stFile.getUrl()).setFilePath(stFile.getFilePath()).setFilename(uuid)
                    .setUploadId(stFile.getUploadId()).setObjectKey(key);
            long end = System.currentTimeMillis();
            log.info("将加密后的文件重新上传耗时：{}(毫秒)", end - start);
        } catch (IOException e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }
        return task;
    }

    /**
     * 分片文件上传
     * @param uploadSplitVO 分片参数
     * @return Result
     */
    public FilePartInfoDTO uploadSplit(UploadSplitVO uploadSplitVO) {
        StFileDTO task = (StFileDTO) ST_FILE_MAP.get(uploadSplitVO.getFileId().toString());
        if (ObjectUtil.isEmpty(task)) {
            task = getByIdentifier(uploadSplitVO.getIdentifier(), uploadSplitVO.getFileId());
        }
        LockInfo lock = lockTemplate.lock("UPLOAD_COMPLETE" + task.getUploadId(), FileConstants.MERRGE_EXPIRE, FileConstants.MERRGE_ACQUIRETIMEOUT);
        if(lock==null){
            log.warn("当前分片正在上传，请稍后");
            throw new SunyardException("文件正在处理中，请稍后重试");
        }
        //处理输入流
        // 读取输入流内容到字节数组
        try {
            byte[] bytes = IOUtils.toByteArray(uploadSplitVO.getInputStream());
            // 使用 ByteArrayInputStream 以支持重复读取
            InputStream repeatableInputStream = new ByteArrayInputStream(bytes);
            uploadSplitVO.setInputStream(repeatableInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            if (ObjectUtil.isEmpty(task)) {
                task = getByIdentifier(uploadSplitVO.getIdentifier(), uploadSplitVO.getFileId());
            }
            uploadSplitVO.setIsEncrypt(task.getIsEncrypt());
            uploadSplitVO.setEncryptKey(task.getEncryptKey());
            uploadSplitVO.setEncryptType(task.getEncryptType());
            uploadSplitVO.setKey(task.getObjectKey());
            //加密第一个分片
            if (StateConstants.IS_ENCRYPT.equals(uploadSplitVO.getIsEncrypt())
                    && uploadSplitVO.getPartNumber() == 1) {
                InputStream inputStream = uploadSplitVO.getInputStream();
                FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStream,
                        uploadSplitVO.getEncryptKey(), uploadSplitVO.getEncryptType());
                EncryptDTO encryptDTO = encryptService.encrypt(fileEncryptInfoDTO);
                inputStream = encryptDTO.getInputStream();
                try {
                    uploadSplitVO.setPartSize((long) inputStream.available());
                    uploadSplitVO.setInputStream(inputStream);
                } catch (IOException e) {
                    log.error("上传分片文件失败", e);
                    throw new RuntimeException("上传分片文件失败", e);
                }
                stFileService.updateFileEncryptLength((long) encryptDTO.getLength(),
                        uploadSplitVO.getFileId());
            }
            FileStorage fileStorage = fileStorageService
                    .getFileStorage(String.valueOf(uploadSplitVO.getEquipmentId()));
            FilePartInfoDTO filePartInfoDTO = fileStorage.uploadSplit(uploadSplitVO);
            return filePartInfoDTO;
        }finally {
            lockTemplate.releaseLock(lock);
        }

    }

    /*** -------------------------------------------------- 分片上传(后端请求OSS服务进行上传) --------------------------------------------------*/
    /**
     * S3后端分片上传
     * @param fileByte 文件
     * @param userId 用户id
     * @param stEquipmentId 设备id
     * @param fileName 文件名
     * @param fileSource 文件来源(服务名：使用spring:application:name)
     * @param md5 文件md5
     * @param isEncrypt 是否加密
     * @return Result
     */
    @Deprecated
    public SysFileDTO useS3Upload(byte[] fileByte, Long userId, Long stEquipmentId, String fileName,
                                  String fileSource, String md5, Integer isEncrypt) {
        long startUpload = System.currentTimeMillis();
        AssertUtils.isNull(stEquipmentId, "存储设备id不能为空");
        AssertUtils.isNull(fileByte, "文件字节流不能为空");
        AssertUtils.isNull(fileSource, "fileSource不能为空");
        int lastIndex = fileName.lastIndexOf(".");
        boolean b = lastIndex != -1 && lastIndex < fileName.length() - 1;
        AssertUtils.isTrue(!b, "文明名需包含后缀");
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        AssertUtils.isNull(suffix, "文明名需包含后缀");
        UploadDTO uploadDTO = new UploadDTO();
        SysFileDTO sysFileDTO = new SysFileDTO();
        InputStream inputStream = null;
        InputStream inputStreamMd5 = null;
        int length = 0;
        try (InputStream inputStream1 = new ByteArrayInputStream(fileByte);
                ByteArrayOutputStream baos = FileUtils.getByteOutputStream(inputStream1)) {
            if (ObjectUtil.isEmpty(md5)) {
                inputStreamMd5 = new ByteArrayInputStream(baos.toByteArray());
                md5 = Md5Utils.calculateMD5(inputStreamMd5);
            }
            String fileName1 = IdUtil.randomUUID();
            String key=getPathKey(fileName1,suffix);
            inputStream = new ByteArrayInputStream(baos.toByteArray());
            long fileSize = inputStream.available();
            //判断是否加密
            String value = null;
            String encryptKey = null;
            Integer encryptType = null;
            if (StateConstants.IS_ENCRYPT.equals(isEncrypt)) {
                //加密文件流
                long start = System.currentTimeMillis();
                value = paramApi.searchValueByKey(StateConstants.FILE_ENCRYPT_TYPE).getData()
                        .getValue();
                encryptKey = StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? storageUploadProperties.getEncryptKey()
                        : null;
                //加密密钥
                encryptKey = Base64Utils.encodeBase64(encryptKey);
                encryptType = StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? 0 : 1;
                FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStream,
                        encryptKey, encryptType);
                EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
                inputStream = dto.getInputStream();
                length = dto.getLength();
                long end = System.currentTimeMillis();
                log.info("加密耗时：" + (end - start) + " ms");
                if (inputStream != null) {
                    fileSize = inputStream.available();
                } else {
                    log.info("加密失败");
                }
            }
            uploadDTO.setInputStream(inputStream);
            uploadDTO.setFileName(fileName);
            uploadDTO.setFileSize(fileSize);
            uploadDTO.setKey(key);
            uploadDTO.setFilePath(key);
            uploadDTO.setChunkSize(5 * 1024 * 1024L);
            uploadDTO.setIsEncrypt(isEncrypt);
            StFileDTO stFile = new StFileDTO();
            FileStorage fileStorage = fileStorageService
                    .getFileStorage(String.valueOf(stEquipmentId));
            stFile = fileStorage.upload(uploadDTO);
            //保存文件信息
            stFile.setChunkNum(Math.toIntExact(fileSize / storageUploadProperties.getChunkSize()))
                    .setChunkSize(storageUploadProperties.getChunkSize())
                    .setSize(fileSize).setFileMd5(md5).setOriginalFilename(fileName)
                    .setFilename(fileName1).setExt(suffix).setObjectKey(key).setCreateUser(userId)
                    .setEquipmentId(stEquipmentId).setSourceFileMd5(md5).setFileSource(fileSource)
                    .setIsUploadOk(FileConstants.FILE_FINISH)
                    //设置是否加密、加密密钥、加密标识符
                    .setIsEncrypt(isEncrypt).setEncryptKey(encryptKey).setEncryptType(encryptType)
                    .setEncryptLen((long) length);
            stFileService.insert(stFile);
            BeanUtils.copyProperties(stFile, sysFileDTO);

            //缓存
            if (storageUploadProperties.getCacheEnable()) {
                InputStream fileStream = fileStorage.getFileStream(stFile.getObjectKey());
                cacheCommonService.cacheFileAsync(fileStream, stFile.getUrl(), stFile.getId(),
                        stFile.getExt(), stFile.getIsEncrypt(), stFile.getEncryptKey(),
                        stFile.getEncryptType(),
                        stFile.getEncryptLen() == null ? 0 : stFile.getEncryptLen().intValue(),null);
            }
            long endUpload = System.currentTimeMillis();
            log.info("完整文件上传耗时:{}", endUpload - startUpload);
        } catch (Exception e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        } finally {
            if (ObjectUtil.isNotEmpty(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭文件流失败", e);
                    throw new RuntimeException(e);
                }
            }
            if (ObjectUtil.isNotEmpty(inputStreamMd5)) {
                try {
                    inputStreamMd5.close();
                } catch (IOException e) {
                    log.error("关闭文件流失败", e);
                    throw new RuntimeException(e);
                }
            }
        }
        return sysFileDTO;
    }

    /**
     * S3后端base64分片上传
     *
     * @param fileBase64 文件
     * @param userId        用户id
     * @param stEquipmentId 设备id
     * @param fileName      文件名
     * @param fileSource    文件来源(服务名：使用spring:application:name)
     * @param md5           文件md5
     * @param isEncrypt     是否加密 加密：1 不加密：其他数字或空
     * @param isFlat
     * @return Result
     */
    public SysFileDTO useS3UploadBase64(String fileBase64, Long userId, Long stEquipmentId,
                                        String fileSource, String fileName, String md5, Integer isEncrypt, Boolean isFlat) {
        MultipartFile file = null;
        try {
            file = Base64Utils.convert(fileBase64, fileName);
        } catch (Exception e) {
            log.error("base64转MultipartFile异常描述", e);
            throw new RuntimeException(e);
        }
        return useS3Upload(file, userId,
                stEquipmentId, fileSource, fileName, md5, isEncrypt, isFlat);
    }

    /**
     * S3后端分片上传
     *
     * @param multipartFile 文件
     * @param userId        用户id
     * @param stEquipmentId 设备id
     * @param fileName      文件名
     * @param fileSource    文件来源(服务名：使用spring:application:name)
     * @param md5           文件md5
     * @param isEncrypt     是否加密 加密：1 不加密：其他数字或空
     * @param isFlat
     * @return Result
     */
    public SysFileDTO useS3Upload(MultipartFile multipartFile, Long userId, Long stEquipmentId,
                                  String fileSource, String fileName, String md5, Integer isEncrypt,
                                  Boolean isFlat) {
        long startUpload = System.currentTimeMillis();
        AssertUtils.isNull(stEquipmentId, "存储设备id不能为空");
        AssertUtils.isNull(multipartFile, "文件流不能为空");
        AssertUtils.isNull(fileSource, "fileSource不能为空");
        //        AssertUtils.isNull(isFlat,"自动纠偏参数isFlat不能为空");
        if (ObjectUtil.isEmpty(isFlat)) {
            isFlat = false;
        }
        if (ObjectUtil.isEmpty(fileName)) {
            fileName = multipartFile.getOriginalFilename();
        }
        int lastIndex = fileName.lastIndexOf(".");
        boolean b = lastIndex != -1 && lastIndex < fileName.length() - 1;
        AssertUtils.isTrue(!b, "文明名需包含后缀");
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        UploadDTO uploadDTO = new UploadDTO();
        SysFileDTO sysFileDTO = new SysFileDTO();
        sysFileDTO.setIsFilePassword(isSingleFilePasswordProtected(multipartFile));
        int length = 0;
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
            if (ObjectUtil.isEmpty(md5)) {
                md5 = Md5Utils.calculateMD5(inputStream);
            }
            //判断是否秒传
            if (isSecondsUpload()) {
                //根据md5查找文件
                StFile stFile = new StFile();
                stFile.setFileMd5(md5);
                List<StFileDTO> list = stFileService.selectFileDTOByPO(stFile);
                if (!CollectionUtils.isEmpty(list)) {
                    BeanUtils.copyProperties(list.get(0), sysFileDTO);
                    return sysFileDTO;
                }
            }
            //            ObjectMetadata objectMetadata = new ObjectMetadata();
            String fileName1 = IdUtil.randomUUID();
            String key=getPathKey(fileName1,suffix);
            //            String contentType = MediaTypeFactory.getMediaType(key).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
            //            objectMetadata.setContentType(contentType);
            long fileSize = multipartFile.getSize();
            //判断是否加密
            String value = null;
            String encryptKey = null;
            Integer encryptType = null;

            //抽取文本
            if(suffix.equals("doc") || suffix.equals("docx")){
                byte[] bytes = WordTextExtractor.copyInputStream(inputStream);
                String text = WordTextExtractor.extractTextHybrid(bytes);
                if (text != null && text.contains("EncryptionInfo")) {
                    sysFileDTO.setIsFilePassword(true);
                }
                sysFileDTO.setContentFirstPage(text);
                inputStream = new ByteArrayInputStream(bytes);
            }

            if (StateConstants.IS_ENCRYPT.equals(isEncrypt)) {
                //加密文件流
                long start = System.currentTimeMillis();
                value = paramApi.searchValueByKey(StateConstants.FILE_ENCRYPT_TYPE).getData()
                        .getValue();
                encryptKey = StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? storageUploadProperties.getEncryptKey()
                        : null;
                //加密密钥
                encryptKey = Base64Utils.encodeBase64(encryptKey);
                encryptType = StateConstants.FILE_ENCRYPT_TYPE_AES.equals(value) ? 0 : 1;
                FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(inputStream,
                        encryptKey, encryptType);
                EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
                inputStream = dto.getInputStream();
                length = dto.getLength();
                long end = System.currentTimeMillis();
                log.info("加密耗时：" + (end - start) + " ms");
                if (inputStream != null) {
                    fileSize = inputStream.available();
                } else {
                    log.info("加密失败");
                }
            }
            uploadDTO.setInputStream(inputStream);
            uploadDTO.setFileName(multipartFile.getOriginalFilename());
            uploadDTO.setFileSize(fileSize);
            uploadDTO.setKey(key);
            uploadDTO.setFilePath(key);
            uploadDTO.setChunkSize(5 * 1024 * 1024L);
            uploadDTO.setIsEncrypt(isEncrypt);
            StFileDTO stFileDTO = new StFileDTO();
            FileStorage fileStorage = fileStorageService
                    .getFileStorageVerify(String.valueOf(stEquipmentId));
            stFileDTO = fileStorage.upload(uploadDTO);
            //保存文件信息
            stFileDTO.setChunkNum(Math.toIntExact(fileSize / storageUploadProperties.getChunkSize()))
                    .setChunkSize(storageUploadProperties.getChunkSize())
                    .setSize(fileSize).setFileMd5(md5).setOriginalFilename(fileName)
                    .setFilename(fileName1).setExt(suffix).setObjectKey(key).setCreateUser(userId)
                    .setEquipmentId(stEquipmentId).setSourceFileMd5(md5)
                    //                    .setBusiBatchNo(UUID.randomUUID().toString())
                    .setFileSource(fileSource).setIsUploadOk(FileConstants.FILE_FINISH)
                    //设置是否加密、加密密钥、加密标识符
                    .setIsEncrypt(isEncrypt).setEncryptKey(encryptKey).setEncryptType(encryptType)
                    .setEncryptLen((long) length);
            stFileService.insert(stFileDTO);
            BeanUtils.copyProperties(stFileDTO, sysFileDTO);
            //去除非必须信息
            sysFileDTO.setBucketName(null);
            sysFileDTO.setFilePath(null);
            sysFileDTO.setObjectKey(null);
            sysFileDTO.setUrl(null);
            //缓存,有密码的文档不缓存
            if (storageUploadProperties.getCacheEnable()
                    && !OnlyOfficeUtil.ifCacheEnable(storageOnlyOfficeProperties.getUseOnlyOffice(), stFileDTO.getExt())
            && !sysFileDTO.getIsFilePassword()) {
                InputStream fileStream = fileStorage.getFileStream(stFileDTO.getObjectKey());
                cacheCommonService.cacheFileAsync(fileStream, stFileDTO.getUrl(), stFileDTO.getId(),
                        stFileDTO.getExt(), stFileDTO.getIsEncrypt(), stFileDTO.getEncryptKey(),
                        stFileDTO.getEncryptType(), stFileDTO.getEncryptLen() == null ? 0
                                : stFileDTO.getEncryptLen().intValue(),null);
            }
            long endUpload = System.currentTimeMillis();
            log.info("完整文件上传耗时:{}", endUpload - startUpload);
        } catch (Exception e) {
            log.error("异常描述", e);
            throw new RuntimeException(e);
        }finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }catch (Exception e){
                log.error("关闭流失败",e);
            }
        }
        return sysFileDTO;
    }

    /**
     * 判断单个文件是否有密码保护（直接接收文件流）
     * @param file 单个上传文件流
     * @return true：文件有密码；false：文件无密码或不支持的文件类型
     */
    public boolean isSingleFilePasswordProtected(MultipartFile file) {
        // 1. 基础参数校验
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 2. 解析文件扩展名（小写处理，避免大小写问题）
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }
        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // 3. 只处理目标文件类型（PDF、DOC、DOCX、XLS、XLSX）
        boolean isSupportType = SunCacheUtils.PDF.contains(fileExt)
                || "doc".equals(fileExt)
                || SunCacheUtils.DOCX.contains(fileExt)
                || "xls".equals(fileExt)
                || "xlsx".equals(fileExt);
        if (!isSupportType) {
            return false;
        }

        // 4. 读取文件流并检测加密（try-with-resources自动关闭流，避免泄漏）
        try (InputStream inputStream = file.getInputStream()) {
            // 根据文件类型尝试加载，无异常则无密码，抛加密异常则有密码
            switch (fileExt) {
                case "pdf":
                    PdfDocument pdf = new PdfDocument();
                    pdf.loadFromStream(inputStream);
                    pdf.close();
                    break;
                case "doc":
                    HWPFDocument doc = new HWPFDocument(inputStream);
                    doc.close();
                    break;
                case "docx":
                    try {
                        XWPFDocument docx = new XWPFDocument(inputStream);
                        docx.close();
                    } catch (OLE2NotOfficeXmlFileException e) {
                        return true;
                    }
                    break;
                case "xls":
                case "xlsx":
                    Workbook workbook = WorkbookFactory.create(inputStream);
                    workbook.close();
                    break;
            }
            // 若上述加载均无异常，说明文件无密码
            return false;

        } catch (EncryptedDocumentException e) {
            // POI库捕获的Excel/Word加密异常 → 有密码
            return true;
        } catch (RuntimeException e) {
            // PDF加密异常（匹配原逻辑中的异常信息）→ 有密码
            log.error("系统异常",e);
            return "can not open an encrypted document. The password is invalid.".equals(e.getMessage());
        } catch (IOException e) {
            // 旧版DOC加密或流异常（通过异常信息判断）→ 有密码
            if (e instanceof FileNotFoundException) {
                return true;
            }else {
                return e.getMessage().contains("encrypted") || e.getMessage().contains("密码");
            }
        }
    }

    /**
     * 支持直接传入InputStream，拷贝流用于检测，不影响原始流
     * @param originalInputStream 原始输入流（检测后仍可正常使用）
     * @param fileName 文件名（用于获取扩展名，判断文件类型）
     * @return true：文件有密码；false：无密码或不支持的类型
     */
    public boolean isInputStreamPasswordProtected(InputStream originalInputStream, String fileName) {
        // 1. 基础参数校验
        if (originalInputStream == null || fileName == null || !fileName.contains(".")) {
            return false;
        }

        // 2. 解析文件扩展名（小写处理）
        String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // 3. 只处理目标文件类型（PDF、DOC、DOCX、XLS、XLSX）
        boolean isSupportType = SunCacheUtils.PDF.contains(fileExt)
                || "doc".equals(fileExt)
                || SunCacheUtils.DOCX.contains(fileExt)
                || "xls".equals(fileExt)
                || "xlsx".equals(fileExt);
        if (!isSupportType) {
            return false;
        }

        // 4. 拷贝原始流到字节数组（核心：避免影响原始流）
        // 用ByteArrayOutputStream缓存流数据，后续可生成多个独立的ByteArrayInputStream
        ByteArrayOutputStream byteCache = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            // 读取原始流并缓存到byteCache（原始流会被读取到末尾，后续需用缓存生成新流）
            while ((len = originalInputStream.read(buffer)) != -1) {
                byteCache.write(buffer, 0, len);
            }
            byteCache.flush();
        } catch (IOException e) {
            // 流读取失败，视为不支持
            return false;
        }

        // 5. 生成用于检测的临时流（从缓存字节数组创建，与原始流无关）
        byte[] fileBytes = byteCache.toByteArray();
        try (InputStream detectStream = new ByteArrayInputStream(fileBytes)) {
            // 根据文件类型检测加密（逻辑与原方法一致，但用的是临时流）
            switch (fileExt) {
                case "pdf":
                    PdfDocument pdf = new PdfDocument();
                    pdf.loadFromStream(detectStream);
                    pdf.close();
                    break;
                case "doc":
                    HWPFDocument doc = new HWPFDocument(detectStream);
                    doc.close();
                    break;
                case "docx":
                    XWPFDocument docx = new XWPFDocument(detectStream);
                    docx.close();
                    break;
                case "xls":
                case "xlsx":
                    Workbook workbook = WorkbookFactory.create(detectStream);
                    workbook.close();
                    break;
            }
            // 无异常 → 无密码
            return false;

        } catch (EncryptedDocumentException e) {
            // Office加密异常 → 有密码
            return true;
        } catch (RuntimeException e) {
            // PDF加密异常（匹配原异常信息）→ 有密码
            log.error("系统异常",e);
            return "can not open an encrypted document. The password is invalid.".equals(e.getMessage());
        } catch (IOException e) {
            // 流异常或旧版DOC加密 → 有密码
            if (e instanceof FileNotFoundException) {
                return true;
            } else {
                return e.getMessage().contains("encrypted") || e.getMessage().contains("密码");
            }
        } finally {
            // 关闭缓存流
            try {
                byteCache.close();
            } catch (IOException e) {
                log.error("系统异常", e);
            }
        }
    }

    /**
     * 取消上传
     * @param md5 文件md5
     * @param stEquipmentId 存储id
     */
    public void cancelFileUpload(String md5, String stEquipmentId) {
        AssertUtils.isNull(md5, "参数错误,文件的md5为空");
        AssertUtils.isNull(stEquipmentId, "参数错误,设备id为空");
        StFileDTO stFile = getByIdentifier(md5, null);
        AssertUtils.isNull(stFile, "参数错误");
        AssertUtils.isNull(stFile.getBucketName(), "参数错误,所属桶名为空");
        AssertUtils.isNull(stFile.getObjectKey(), "参数错误，桶下的文件路径为空");
        AssertUtils.isNull(stFile.getUploadId(), "参数错误,上传id为空");
        FileStorage fileStorage = fileStorageService.getFileStorage(String.valueOf(stEquipmentId));
        fileStorage.cancelFileUpload(stFile.getBucketName(), stFile.getObjectKey(),
                stFile.getUploadId());
        log.info("文件上传已取消,已经上传的分片已被删除");
    }

    /**
     * -------------------------------------------------- 私有方法 --------------------------------------------------
     */

    /**
     * 偏离矫正
     *
     * @param task        文件对象
     * @param fileStorage 文件存储
     * @return StFile
     */
    private StFileDTO pictureCorrection(StFileDTO task, FileStorage fileStorage) {
        InputStream inputStreamFromUrl = null;
        inputStreamFromUrl = fileStorage.getFileStream(task.getObjectKey());
        InputStream inputStream = null;
        if (!"false".equals(storageUploadProperties.getPythonDir())) {
            inputStream = ImgPythonCheckUtils.rectifyPic(inputStreamFromUrl, null, storageUploadProperties.getPythonDir(),
                    task.getExt());
        } else {
            inputStream = pictureCorrection(inputStream);
        }
        AssertUtils.isNull(inputStream, "偏离矫正有误");
        String uuid = IdUtil.randomUUID();
        String key=getPathKey(uuid,task.getExt());
        UploadDTO uploadDTO = null;
        try {
            uploadDTO = new UploadDTO().setInputStream(inputStream).setKey(key)
                    .setFileSize((long) inputStream.available()).setFilePath(task.getObjectKey())
                    .setChunkSize(storageUploadProperties.getChunkSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //替换文件
        StFileDTO stFile = fileStorage.replaceFile(uploadDTO);
        task.setUrl(stFile.getUrl()).setFilePath(stFile.getFilePath()).setFilename(uuid)
                .setUploadId(stFile.getUploadId()).setObjectKey(key);
        return task;
    }

    /**
     * 偏离矫正
     *
     * @param inputStreamFromUrl 文件流
     * @return StFile
     */
    private InputStream pictureCorrection(InputStream inputStreamFromUrl) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //读取InputStream并转换为OpenCV的Mat对象
        byte[] byteArray = new byte[0];
        byteArray = FileUtils.read(inputStreamFromUrl);
        // 使用OpenCV将字节数组转换为Mat
        Mat m = Imgcodecs.imdecode(new MatOfByte(byteArray), Imgcodecs.IMREAD_COLOR);

        //偏离矫正
        Mat mat = RectifyImageUtils.imgCorrection(m, byteArray);
        //保存的二进制数据
        MatOfByte b = new MatOfByte();
        //Mat转换成二进制数据。.png表示图片格式，格式不重要，基本不会对程序有任何影响。
        Imgcodecs.imencode(".png", mat, b);
        //二进制数据转换成Image
        return new ByteArrayInputStream(b.toArray());
    }

    /**
     * 判断时候进行秒传
     *
     * @return Result
     */
    private Boolean isSecondsUpload() {
        Result<SysParamDTO> paramDtoResult = paramApi
                .searchValueByKey("FILE_UPLOAD_SECOND_PASS_SWITCH");
        if (ObjectUtil.isNotEmpty(paramDtoResult.getData())
                && "0".equals(paramDtoResult.getData().getValue())) {
            return true;
        }
        return false;
    }

    /**
     * 获取文件对象
     *
     * @param identifier md5
     * @param id         id
     * @return StFile
     */
    private List<StFileDTO> getByIdentifierAndId(String identifier, Long id) {
        StFile stFile = new StFile();
        stFile.setSourceFileMd5(identifier);
        stFile.setId(id);
        List<StFileDTO> stFiles = stFileService.selectFileDTOByPO(stFile);
        if (!CollectionUtils.isEmpty(stFiles)) {
            //查询分片表
            return stFiles;
        }
        return null;
    }

    /**
     * 物理删除文件上传信息
     */
    public void physicalDeleteByFileId(Long fileId) {
        // 执行物理删除
        stSplitUploadMapper.physicalDeleteByFileId(fileId);
    }


    @Async("FileCacheThreadPool")
    public void asyncSplitPdfToImages(FileSplitPdfVO ecmSplitPdfVo, byte[] pdfBytes, String redisKey, int totalPage, String lockRedisKey, String directory){
        LockInfo lockInfo = null;
        try {
            lockInfo = lockTemplate.lock(lockRedisKey, expire, acquireTimeout);
            int pageSize = 10;
            if (lockInfo != null) {
                for (int i = 0; i < totalPage; i+= pageSize) {
                    int endPage = Math.min(i + pageSize, totalPage);
                    int pageNum = i/ecmSplitPdfVo.getSplitPageSize();
                    log.info("当前异步处理：{} 到 {}页的数据",i, endPage);
                    SplitPdfUtils.convertPdfToImagesParallel(pdfBytes, ecmSplitPdfVo.getSplitPageSize(), pageNum, totalPage, ecmSplitPdfVo.getFileMd5(), directory);
                }
                log.info("异步处理完成！");
            }
        }catch (Exception e){
            log.error("pdf拆分错误:",e);
        } finally {
            if (lockInfo != null) {
                lockTemplate.releaseLock(lockInfo);
            }
        }
    }

    @Async("FileCacheThreadPool")
    public void asyncSplitPdfToImagesBySync(FileSplitPdfVO ecmSplitPdfVo, byte[] pdfBytes, int totalPage, String lockRedisKey,String directory) {
        LockInfo lockInfo = null;
        PdfDocument sourceDoc1 = null;
        try {
            lockInfo = lockTemplate.lock(lockRedisKey, expire, acquireTimeout);
            if (lockInfo != null) {
                int pageSize = 10;
                //大文件只加载一次
                sourceDoc1 = new PdfDocument(new ByteArrayInputStream(pdfBytes));
                for (int i = 0; i < totalPage; i += pageSize) {
                    int endPage = Math.min(i + pageSize, totalPage);
                    SplitPdfUtils.convertPdfToImagesParallelWithOutExecutor(i, sourceDoc1, totalPage, endPage,directory,ecmSplitPdfVo.getFileMd5());
                }
                log.info("异步处理完成！");
            }
        } catch (Exception e) {
            log.error("pdf拆分错误:", e);
        } finally {
            if (lockInfo != null) {
                lockTemplate.releaseLock(lockInfo);
            }
            if (sourceDoc1 != null) {
                sourceDoc1.close();
            }
        }
    }


    public void storeRedis(ArrayList<String> base64List, String pdfName, String redisKey, Integer startPage) {
        Map<String, Object> redisHash = Stream.iterate(0, i -> i + 1)
                .limit(base64List.size())
                .collect(Collectors.toMap(
                        i -> pdfName + "_" + (startPage+i),
                        base64List::get
                ));
        redisUtil.hmset(redisKey,redisHash, TimeOutConstants.SEVEN_DAY);
    }
    /**
     * 根据配置获取hash存储key或日志存储Key
     */
    public String getPathKey(String uuid,String ext){
        String key;
        if(FileConstants.DATE.equals(storageUploadProperties.getKeyType())){
            key = StrUtil.format("{}/{}.{}", DateUtil.format(new Date(), "yyyy-MM-dd"), uuid,
                    ext);
        }else if (FileConstants.HASH.equals(storageUploadProperties.getKeyType())){
            try {
                HashPathGeneratorUtils generator = new HashPathGeneratorUtils("");
                key = generator.generatePath(uuid, ext);
            }catch (Exception e){
                log.error("系统异常", e);
                throw new RuntimeException("获取存储路径hashKey失败", e);
            }
        }else {
            //默认按日期
            key = StrUtil.format("{}/{}.{}", DateUtil.format(new Date(), "yyyy-MM-dd"), uuid,
                    ext);
        }
        return key;
    }

}
