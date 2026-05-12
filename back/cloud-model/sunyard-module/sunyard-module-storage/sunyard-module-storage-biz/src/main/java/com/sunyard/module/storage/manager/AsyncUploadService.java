package com.sunyard.module.storage.manager;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sunyard.module.storage.config.factory.FileStorage;
import com.sunyard.module.storage.config.factory.impl.FileStorageService;
import com.sunyard.module.storage.dto.UploadDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zyl
 * @Description
 * @since 2023/12/5 17:22
 */
@Slf4j
@Service
public class AsyncUploadService {
    @Resource
    private FileStorageService fileStorageService;

    /**
     * 上传
     * @param fileName2 文件名称
     * @param equipmentId 设备id
     * @param key 文件key
     * @param isEncrypt 是否加密
     * @param mergedPdfStream 文件输入流
     */
    @Async("GlobalThreadPool")
    public void useS3Upload(String fileName2, Long equipmentId, String key, Integer isEncrypt,
                            InputStream mergedPdfStream) {
        log.info("当前线程名称：{},校验数据开始时间：{}", Thread.currentThread().getName(), LocalDateTime.now());
        //获取对应的存储平台
        FileStorage fileStorage = fileStorageService.getFileStorage(String.valueOf(equipmentId));
        UploadDTO uploadDTO = new UploadDTO();
        uploadDTO.setInputStream(mergedPdfStream);
        uploadDTO.setFileName(fileName2);
        try {
            uploadDTO.setFileSize((long) mergedPdfStream.available());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException(e);
        }
        uploadDTO.setKey(key);
        uploadDTO.setFilePath(key);
        uploadDTO.setChunkSize(5 * 1024 * 1024L);
        uploadDTO.setIsEncrypt(isEncrypt);
        //上传
        fileStorage.upload(uploadDTO);
    }

}
