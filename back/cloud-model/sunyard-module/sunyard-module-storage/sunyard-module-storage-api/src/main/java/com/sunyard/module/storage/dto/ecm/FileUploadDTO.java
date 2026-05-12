package com.sunyard.module.storage.dto.ecm;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: lw
 * @Date: 2024/1/28
 * @Description: 文件上传DTO
 */
@Data
public class FileUploadDTO {
    /**
     * 文件
     */
    private MultipartFile file;
    /**
     * 设备id
     */
    private Long stEquipmentId;
    /**
     * 上传人id
     */
    private Long userId;
    /**
     * 文件来源(服务名：使用spring:application:name)
     */
    private String fileSource;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 文件md5
     */
    private String md5;

    /**
     * 是否加密  0：不加密 1：加密
     */
    private Integer isEncrypt;
}
