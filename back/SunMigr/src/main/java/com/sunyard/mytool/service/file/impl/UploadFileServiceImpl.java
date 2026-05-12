package com.sunyard.mytool.service.file.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.sunyard.mytool.constant.MigrateConstant;
import com.sunyard.mytool.dto.EncryptDTO;
import com.sunyard.mytool.dto.FileEncryptInfoDTO;
import com.sunyard.mytool.dto.UploadDTO;
import com.sunyard.mytool.entity.DocTemp;
import com.sunyard.mytool.entity.StEquipment;
import com.sunyard.mytool.entity.StFile;
import com.sunyard.mytool.entity.ecm.FileTemp;
import com.sunyard.mytool.service.file.EncryptService;
import com.sunyard.mytool.service.file.FileStroageService;
import com.sunyard.mytool.service.file.UploadFileService;
import com.sunyard.mytool.until.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.Future;

@Slf4j
@Service
public class UploadFileServiceImpl implements UploadFileService {

    @Value("${equipmentId}")
    private Long equipmentId = 0L;

    @Value("${fileRootPath}")
    private String fileRootPath = "";
    @Value("${versionPath}")
    private String versionPath;

    @Value("${isuploadfile:1}")
    private Integer IS_UPLOAD_FILE= 1;
    @Value("${upload.isencrypt:1}")
    private Integer IS_ENCRYPT= 1;
    @Value("${upload.encrypt.encrypt-type:AES}")
    private String ENCRYPT_TYPE= "AES";
    @Autowired
    private IDUtils idUtils;
    //加密密钥
    String encryptKey = "8a08826984224746";

    @Autowired
    private EncryptService encryptService;
    @Autowired
    private FileStroageServiceManager fileStroageServiceManager;

    /**
     * EDM上传文件
     */
    @Override
    public StFile uploadEDMFile(DocTemp docTemp) {
        log.info("开始上传文件, 中间表id: {} , 文件名称: {}", docTemp.getPkId(),docTemp.getFileName());
        StEquipment stEquipment = fileStroageServiceManager.getStEquipment(equipmentId);
        //取文件后缀
        String suffix = docTemp.getFileExt();
        String ext = suffix != null && suffix.startsWith(".") ? suffix.substring(1) : suffix;
        String uuId = IdUtil.randomUUID();
        //String objectKey = StrUtil.format("{}/{}.{}", DateUtil.format(upTime, "YYYY-MM-dd"), uuId, ext);
        String objectKey = getPathKey(uuId, suffix);
        //组装stfile
        StFile stFile = new StFile();
        stFile.setId(IdUtil.getSnowflake().nextId());
        stFile.setOriginalFilename(docTemp.getFileName());
        stFile.setFilename(uuId);
        stFile.setExt(ext);
        stFile.setObjectKey(objectKey);
        stFile.setFileSource("edm-migr");
        stFile.setEquipmentId(equipmentId);
        stFile.setCreateTime(new Date());
        stFile.setIsDeleted(0);
        stFile.setIsEncrypt(0);

        //拼接源文件路径  是否历史文件 0:否,1:是
        String sourceFilePath = "";
        if ("1".equals(docTemp.getIsHistory())){
            sourceFilePath = Paths.get(versionPath, docTemp.getTxPath()).toString();
        }else {
            sourceFilePath = Paths.get(fileRootPath, docTemp.getTxPath()).toString();
        }
        log.info("拼接源文件路径：{}", sourceFilePath);

        File file = new File(sourceFilePath);

        InputStream fileStream = null;
        InputStream sourceStream = null;
        long fileSize;
        String fileMd5 = "";
        int length = 0;
        try {
            // 计算MD5
            sourceStream = new BufferedInputStream(new FileInputStream(file));
            // 标记流的位置
            sourceStream.mark(Integer.MAX_VALUE);
            fileMd5 = MD5Util.getMD5(sourceStream);
            // 重置流到标记的位置
            sourceStream.reset();
            fileSize = sourceStream.available();
            fileStream = sourceStream;
            if (fileSize <= 0) {
                throw new RuntimeException("文件异常:文件大小为0KB");
            }
            stFile.setSize(fileSize);

            //加密文件
            /*long start = System.currentTimeMillis();
            //加密密钥
            encryptKey = Base64Utils.encodeBase64(encryptKey);

            //encryptType 传0 为AES加密，传1 为SM2
            FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(fileStream, encryptKey, 0);
            EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
            fileStream = dto.getInputStream();
            length = dto.getLength();
            long end = System.currentTimeMillis();

            if (fileStream != null) {
                fileSize = fileStream.available();
                log.info("id: " + docTemp.getPkId() + " 加密耗时：" + (end - start) + " ms");
            } else {
                log.error("文件加密失败, id: {}", docTemp.getPkId());
                throw new RuntimeException("文件加密失败");
            }*/
            stFile.setIsEncrypt(0);
            //stFile.setEncryptKey(encryptKey);
            //stFile.setEncryptType(0);
            //stFile.setEncryptLen((long) length);
            stFile.setSourceFileMd5(fileMd5);
            stFile.setFileMd5(fileMd5);

            try {
                //上传文件
                long startTime = System.currentTimeMillis();
                FileStroageService fileStroageService = fileStroageServiceManager.getFileStroage(stEquipment);
                UploadDTO uploadDTO = new UploadDTO();
                uploadDTO.setInputStream(fileStream);
                uploadDTO.setKey(objectKey);
                uploadDTO.setChunkSize(5 * 1024 * 1024L);
                uploadDTO.setFileSize(fileSize);
                uploadDTO.setBasePath(stEquipment.getBasePath());
                fileStroageService.upload(uploadDTO);
                log.info("*文件上传成功*: {}, 耗时: {} ms", docTemp.getPkId(), System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                log.error("文件上传失败, fileid: {}", docTemp.getPkId(), e);
                throw new RuntimeException("文件上传失败");
            }

        } catch (FileNotFoundException e) {
            log.error("文件不存在，中间表id: {}", docTemp.getPkId(), e);
            throw new RuntimeException("文件异常:文件不存在");
        } catch (IOException e) {
            log.error("文件流读取失败，中间表id: {}", docTemp.getPkId(), e);
            throw new RuntimeException("文件读取异常:文件流获取失败");
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    throw new RuntimeException("文件流关闭失败 " + e);
                }
            }
            if (sourceStream != null) {
                try {
                    sourceStream.close();
                } catch (IOException e) {
                    throw new RuntimeException("文件流关闭失败 " + e);
                }
            }
        }
        return stFile;
    }

    /**
     *
     * ECM异步上传文件
     * @return
     */
    @Override
    @Async("uploadExecutor")
    public Future<StFile> asyncUploadFile(StEquipment stEquipment, FileTemp fileTemp, FileStroageService fileStroageService) {
        StFile uploadedFile = uploadFile(stEquipment, fileTemp, fileStroageService);
        return new AsyncResult<>(uploadedFile);
    }

    public StFile uploadFile(StEquipment stEquipment, FileTemp fileTemp, FileStroageService fileStroageService) {
        InputStream fileStream = null;
        InputStream sourceStream = null;
        long fileSize;
        String fileMd5 = "";
        Integer cloudType = fileTemp.getCloudType();
        //路径参数都从中间表取保证同一文件生成的路径一样
        String originalName = fileTemp.getFileName();
        String ext = fileTemp.getFileExt();
        String uuId = IdUtil.randomUUID();
        //String objectKey = StrUtil.format("{}/{}.{}", DateUtil.format(upTime, "YYYY-MM-dd"), uuId, ext);
        String objectKey = getPathKey(uuId, ext);
        //组装stfile
        StFile stFile = new StFile();
        stFile.setId(idUtils.nextId());
        stFile.setOriginalFilename(originalName);
        stFile.setFilename(uuId);
        stFile.setExt(ext);
        //stFile.setSize(fileTemp.getFileSize());
        if (MigrateConstant.IS_UPLOADFILE.equals(IS_UPLOAD_FILE)) {
            stFile.setObjectKey(objectKey);
        } else {
            String relativePath ="";
            String basePath = stEquipment.getBasePath();
            String filePath = fileTemp.getFilePath();
            if (filePath.startsWith(basePath)) {
                relativePath = filePath.substring(basePath.length());
                // 如果 relativePath 以 "/" 开头，则去掉开头的 "/"
                if (relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
            } else {
                // 不匹配
                throw new RuntimeException("文件路径与basepath: " + basePath + "不匹配");
            }
            stFile.setObjectKey(relativePath);
        }
        stFile.setFileSource("ecm-migr");
        stFile.setEquipmentId(stEquipment.getId());
        stFile.setIsDeleted(0);
        stFile.setIsEncrypt(0);
        stFile.setEncryptKey("");
        stFile.setEncryptType(null); // 加密算法按微服务的来
        stFile.setEncryptLen(null);

        String encryptKey = null;
        Integer encryptType = null;
        int length = 0;

        //拿源文件
        try {
            if (cloudType == MigrateConstant.LOCAL_STORAGE) {
                //拼接源文件路径 是否历史文件 0:否,1:是
                String sourceFilePath = "";
                if ("1".equals(fileTemp.getIsHistory())){
                    sourceFilePath = Paths.get(versionPath, fileTemp.getFilePath()).toString();
                }else {
                    sourceFilePath = Paths.get(fileRootPath, fileTemp.getFilePath()).toString();
                }

                File file = new File(sourceFilePath);

                // 先处理解密和MD5计算
                if (MigrateConstant.IS_ENCRYPT.equals(fileTemp.getIsEncrypt())) {
                    // 如果文件是加密的，先解密并计算MD5
                    sourceStream = new BufferedInputStream(new FileInputStream(file));
                    fileStream = FileEncryptUtils.reverseFile(sourceStream); //这里是老影像的解密
                    // 标记流的位置
                    fileStream.mark(Integer.MAX_VALUE);
                    fileMd5 = MD5Util.getMD5(fileStream);
                    // 重置流到标记的位置
                    fileStream.reset();
                    fileSize = fileStream.available();

                } else {
                    // 文件未加密，直接计算MD5
                    sourceStream = new BufferedInputStream(new FileInputStream(file));
                    // 标记流的位置
                    sourceStream.mark(Integer.MAX_VALUE);
                    fileMd5 = MD5Util.getMD5(sourceStream);
                    // 重置流到标记的位置
                    sourceStream.reset();
                    fileSize = sourceStream.available();
                    fileStream = sourceStream;

                }
                if (fileSize <= 0) {
                    throw new RuntimeException("文件异常:文件大小为0KB");
                }
                stFile.setSize(fileSize);
                //判断是否加密
                if (MigrateConstant.IS_ENCRYPT.equals(IS_ENCRYPT) && MigrateConstant.IS_UPLOADFILE.equals(IS_UPLOAD_FILE)) {
                    //加密文件流
                    long start = System.currentTimeMillis();
                    encryptKey = MigrateConstant.FILE_ENCRYPT_TYPE_AES.equals(ENCRYPT_TYPE) ? this.encryptKey
                            : null;
                    //加密密钥
                    encryptKey = Base64Utils.encodeBase64(encryptKey);
                    encryptType = MigrateConstant.FILE_ENCRYPT_TYPE_AES.equals(ENCRYPT_TYPE) ? 0 : 1;
                    FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(fileStream,
                            encryptKey, encryptType);
                    EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
                    fileStream = dto.getInputStream();
                    length = dto.getLength();
                    long end = System.currentTimeMillis();

                    if (fileStream != null) {
                        fileSize = fileStream.available();
                        log.info("fileid: " + fileTemp.getFileId() + " 加密耗时：" + (end - start) + " ms");
                    } else {
                        log.error("文件加密失败, fileid: {}", fileTemp.getFileId());
                        throw new RuntimeException("文件加密失败");
                        //log.info("加密失败");
                    }
                    stFile.setIsEncrypt(1);
                    stFile.setEncryptKey(encryptKey);
                    stFile.setEncryptType(encryptType);
                    stFile.setEncryptLen((long) length);
                }
                stFile.setSourceFileMd5(fileMd5);
                stFile.setFileMd5(fileMd5);
            } else {
                // 从远程存储获取流
                FileStroageService fileStroage = null;
                try {
                    fileStroage = fileStroageServiceManager.getFileStroagebyBucket(fileTemp.getBucket());
                } catch (Exception e) {
                    throw new RuntimeException("未找到该存储设备,bucket: " + fileTemp.getBucket());
                }
                // 先处理解密和MD5计算
                if (MigrateConstant.IS_ENCRYPT.equals(fileTemp.getIsEncrypt())) {
                    sourceStream = fileStroage.getFileStream(fileTemp.getBucket(), fileTemp.getFilePath());
                    fileStream = FileEncryptUtils.reverseFile(sourceStream); //这里是老影像的解密
                    // 标记流的位置
                    fileStream.mark(Integer.MAX_VALUE);
                    fileMd5 = MD5Util.getMD5(fileStream);
                    // 重置流到标记的位置
                    fileStream.reset();
                    fileSize = fileStream.available();
                } else {
                    //文件未加密，直接计算MD5
                    sourceStream = fileStroage.getFileStream(fileTemp.getBucket(), fileTemp.getFilePath());
                    // 标记流的位置
                    sourceStream.mark(Integer.MAX_VALUE);
                    fileMd5 = MD5Util.getMD5(sourceStream);
                    // 重置流到标记的位置
                    sourceStream.reset();
                    fileSize = sourceStream.available();
                    fileStream = sourceStream;

                }
                if (fileSize <= 0) {
                    throw new RuntimeException("文件异常:文件大小为0KB");
                }
                stFile.setSize(fileSize);
                //判断是否加密
                if (MigrateConstant.IS_ENCRYPT.equals(IS_ENCRYPT) && MigrateConstant.IS_UPLOADFILE.equals(IS_UPLOAD_FILE)) {
                    //加密文件流
                    long start = System.currentTimeMillis();
                    encryptKey = MigrateConstant.FILE_ENCRYPT_TYPE_AES.equals(ENCRYPT_TYPE) ? this.encryptKey
                            : null;
                    //加密密钥
                    encryptKey = Base64Utils.encodeBase64(encryptKey);
                    encryptType = MigrateConstant.FILE_ENCRYPT_TYPE_AES.equals(ENCRYPT_TYPE) ? 0 : 1;
                    FileEncryptInfoDTO fileEncryptInfoDTO = new FileEncryptInfoDTO(fileStream,
                            encryptKey, encryptType);
                    EncryptDTO dto = encryptService.encrypt(fileEncryptInfoDTO);
                    fileStream = dto.getInputStream();
                    length = dto.getLength();
                    long end = System.currentTimeMillis();
                    log.info("中间表数据fileid: " + fileTemp.getFileId() + " 加密耗时：" + (end - start) + " ms");
                    if (fileStream != null) {
                        fileSize = fileStream.available();
                        log.info("fileid: " + fileTemp.getFileId() + " 加密耗时：" + (end - start) + " ms");
                    } else {
                        log.error("文件加密失败, fileid: {}", fileTemp.getFileId());
                        throw new RuntimeException("文件加密失败");
                        //log.info("加密失败");
                    }
                    stFile.setIsEncrypt(1);
                    stFile.setEncryptKey(encryptKey);
                    stFile.setEncryptType(encryptType);
                    stFile.setEncryptLen((long) length);
                }
                stFile.setSourceFileMd5(fileMd5);
                stFile.setFileMd5(fileMd5);
            }
        } catch (FileNotFoundException e) {
            log.error("文件不存在，中间表fileId: {}", fileTemp.getFileId(), e);
            throw new RuntimeException("文件异常:文件不存在");
        } catch (IOException e) {
            log.error("文件流读取失败，中间表fileId: {}", fileTemp.getFileId(), e);
            throw new RuntimeException("文件读取异常:文件流获取失败");
        }
        try {
            //是否迁移文件
            if (MigrateConstant.IS_UPLOADFILE.equals(IS_UPLOAD_FILE)) {
                long startTime = System.currentTimeMillis();
                UploadDTO dto = new UploadDTO();
                dto.setInputStream(fileStream);
                dto.setKey(objectKey);
                dto.setChunkSize(5 * 1024 * 1024L);
                dto.setFileSize(fileSize);
                dto.setBasePath(stEquipment.getBasePath());
                fileStroageService.upload(dto);
                log.info("*文件上传成功*: {}, 耗时: {} ms", fileTemp.getFileId(), System.currentTimeMillis() - startTime);
            }
        } catch (Exception e) {
            log.error("文件上传发生错误, fileid: {}", fileTemp.getFileId(), e);
            throw new RuntimeException("文件上传发生错误");
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    throw new RuntimeException("文件流关闭失败 " + e);
                }
            }
            if (sourceStream != null) {
                try {
                    sourceStream.close();
                } catch (IOException e) {
                    throw new RuntimeException("文件流关闭失败 " + e);
                }
            }
        }
        return stFile;
    }

    /**
     * 根据配置获取hash存储key或日志存储Key
     */
    public String getPathKey(String uuid, String ext) {
        String key;
        try {
            HashPathGeneratorUtils generator = new HashPathGeneratorUtils("");
            key = generator.generatePath(uuid, ext);
        } catch (Exception e) {
            log.error("系统异常", e);
            throw new RuntimeException("获取存储路径hashKey失败", e);
        }

        return key;
    }
}
