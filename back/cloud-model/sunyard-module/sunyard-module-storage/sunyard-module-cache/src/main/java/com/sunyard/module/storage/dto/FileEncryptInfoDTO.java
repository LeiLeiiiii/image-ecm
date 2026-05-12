package com.sunyard.module.storage.dto;

import lombok.Data;

import java.io.InputStream;

/**
 * <p>
 * 加密文件信息DTO
 * </p>
 *
 * @author yzy
 * @since 2024-11-04
 */
@Data
public class FileEncryptInfoDTO {

    /**
     * 文件流
     */
    private InputStream inputStream;

    /**
     * 密钥
     */
    private String encryptKey;

    /**
     * 加密类型,0为AES
     */
    private Integer encryptType;

    /**
     * 加密长度
     */
    private Integer encryptLength;

    //加密用
    public FileEncryptInfoDTO(InputStream inputStream, String encryptKey, Integer encryptType) {
        this.inputStream = inputStream;
        this.encryptKey = encryptKey;
        this.encryptType = encryptType;
    }


    //解密用
    public FileEncryptInfoDTO(InputStream inputStream, String encryptKey, Integer encryptType, Integer encryptLength) {
        this.inputStream = inputStream;
        this.encryptKey = encryptKey;
        this.encryptType = encryptType;
        this.encryptLength = encryptLength;
    }
}
