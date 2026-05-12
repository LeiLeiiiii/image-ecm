package com.sunyard.module.storage.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.InputStream;

/**
 * @author zyl
 * @Description
 * @since 2024/3/20 9:25
 */
@Data
@Accessors(chain = true)
public class UploadDTO {
    /**
     * 文件流
     */
    private InputStream inputStream;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件上传路径
     */
    private String key;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 分片大小
     */
    private Long chunkSize;

    /**
     * 文件唯一标识
     */
    private String md5;

    /**
     * 文件相对路径
     */
    private String filePath;

    /**
     * 是否加密  0：不加密 1：加密
     */
    private Integer isEncrypt;


}
