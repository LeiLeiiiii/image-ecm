package com.sunyard.framework.ocr.service;
/*
 * Project: Sunyard
 *
 * File Created at 2025/10/10
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.benjaminwan.ocrlibrary.OcrResult;
import com.sunyard.framework.ocr.config.properties.OcrProperties;

import io.github.mymonstercat.ocr.InferenceEngine;
import lombok.extern.slf4j.Slf4j;

/**
 * https://github.com/MyMonsterCat/RapidOcr-Java
 * CentOS7无法运行：请参考CentOS7升级GCC
 * @author Leo
 * @Desc 飞浆 ocr的java版本封装
 * @date 2025/10/10 18:38
 */
@Slf4j
@Service
public class RapidOcrSerivce {

    @Resource
    private InferenceEngine engine;
    @Resource
    private OcrProperties ocrProperties;

    private static final ReentrantLock DIR_LOCK = new ReentrantLock();

    // ==================== 公共 API ====================

    public String recognize(String input) throws IOException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be null or empty");
        }
        input = input.trim();

        if (input.startsWith("http://") || input.startsWith("https://")) {
            // 使用 try-with-resources 确保 URL 流关闭
            try (InputStream is = new URL(input).openStream()) {
                return recognizeFromStream(is);
            }
        } else {
            Path localPath = Paths.get(input);
            if (!Files.exists(localPath)) {
                throw new FileNotFoundException("Local file not found: " + input);
            }
            return doOcr(input);
        }
    }

    public String recognize(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }
        return doOcr(file.getAbsolutePath());
    }

    public String recognize(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        return recognizeFromStream(inputStream);
    }

    public String recognize(byte[] imageBytes) throws IOException {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Image bytes cannot be null or empty");
        }
        try (InputStream is = new ByteArrayInputStream(imageBytes)) {
            return recognizeFromStream(is);
        }
    }

    // ==================== 核心逻辑 ====================

    private String recognizeFromStream(InputStream inputStream) throws IOException {
        Path tempFile = null;
        try {
            tempFile = writeToTempFile(inputStream);
            return doOcr(tempFile.toString());
        } finally {
            // 无论成功/失败/异常，都删除临时文件
            if (tempFile != null) {
                deleteQuietly(tempFile);
            }
        }
    }

    private String doOcr(String path) throws IOException {
        OcrResult result = engine.runOcr(path);
        if (result == null) {
            throw new IOException("OCR engine returned null result");
        }
        String text = result.getStrRes();
        return text != null ? text.trim() : "";
    }

    private Path writeToTempFile(InputStream inputStream) throws IOException {
        Path tempDir = Paths.get(ocrProperties.getRapidOcrFileTempDir());
        if (!Files.exists(tempDir)) {
            DIR_LOCK.lock();
            try {
                if (!Files.exists(tempDir)) {
                    try {
                        Files.createDirectories(tempDir);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to create temp directory: "+ocrProperties.getRapidOcrFileTempDir(),
                                e);
                    }
                }
            } finally {
                DIR_LOCK.unlock();
            }
        }
        String prefix = "ocr_" + UUID.randomUUID().toString().substring(0, 12) + "_";
        Path tempFile = Files.createTempFile(tempDir, prefix, ".tmp");

        try (OutputStream out = Files.newOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    private void deleteQuietly(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.error("Failed to delete temp file: " + path + " - :" ,e);
            }
        }
    }
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/10/10 Leo creat
 */
