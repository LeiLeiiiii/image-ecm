package com.sunyard.mytool.dto;

import lombok.Data;

import java.io.InputStream;

/**
 * <p>
 * 加解密返回DTO
 * </p>
 */
@Data
public class EncryptDTO {
    private InputStream inputStream;
    private int length;
}
