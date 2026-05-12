package com.sunyard.module.storage.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zyl
 * @Description
 * @since 2023/11/29 17:19
 */
@Data
public class UploadVO {
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
     * 文件名称
     */
    private String md5;

}
