package com.sunyard.module.storage.dto;

import lombok.Data;

import java.io.InputStream;

/**
 * <p>
 * 加解密返回DTO
 * </p>
 *
 * @author yzy
 * @since 2024-11-04
 */
@Data
public class EncryptDTO {
    private InputStream inputStream;
    private int length;
}
