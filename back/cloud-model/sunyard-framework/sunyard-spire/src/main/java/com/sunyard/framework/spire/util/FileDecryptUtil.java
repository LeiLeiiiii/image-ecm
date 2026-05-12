package com.sunyard.framework.spire.util;
/*
 * Project: Sunyard
 *
 * File Created at 2023/7/12
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.spire.doc.Document;
import com.spire.pdf.PdfDocument;
import com.spire.presentation.Presentation;
import com.spire.xls.FileFormat;
import com.spire.xls.Workbook;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Leo
 * @Desc
 * @date 2023/7/12 9:38
 */
@Slf4j
public class FileDecryptUtil {


    // 支持的解密文件格式（后缀名，小写）
    public static final List<String> SUPPORTED_FORMATS = Arrays.asList(
            "pdf",         //pdf
            "doc", "docx", // Word
            "xls", "xlsx", // Excel
            "ppt", "pptx"  // PPT
    );
    /**
     * 校验pfd
     *
     * @param filePath 文件地址
     * @return Result
     */
    public static boolean checkPdf(String filePath) {
        boolean result = PdfDocument.isPasswordProtected(filePath);
        return result;
    }

    /**
     * 解密PDF输入流，返回解密后的输入流，并自动关闭原始输入流
     * @param inputStream 加密的PDF输入流（需关闭）
     * @param password PDF解密密码
     * @return 解密后的PDF输入流（需调用方使用后关闭）
     * @throws IOException 处理过程中的IO异常
     */
    public static InputStream decryptPdf(InputStream inputStream, String password) throws IOException {
        PdfDocument pdfDocument = null;
        ByteArrayOutputStream outputStream = null;
        try {
            // 1. 初始化文档并加载加密的PDF流（使用密码解密）
            pdfDocument = new PdfDocument();
            pdfDocument.loadFromStream(inputStream, password);

            // 2. 清除加密设置
            pdfDocument.decrypt();

            // 3. 将解密后的PDF写入字节输出流
            outputStream = new ByteArrayOutputStream();
            pdfDocument.saveToStream(outputStream);
            outputStream.flush(); // 确保数据写入完成

            // 4. 将字节流转为输入流返回（调用方需负责关闭此流）
            return new ByteArrayInputStream(outputStream.toByteArray());

        } finally {
            // 5. 强制关闭所有资源（原始输入流、文档、输出流）
            if (inputStream != null) {
                try {
                    inputStream.close(); // 关闭原始输入流
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
            if (pdfDocument != null) {
                pdfDocument.close(); // 关闭PDF文档
            }
            if (outputStream != null) {
                try {
                    outputStream.close(); // 关闭输出流
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
        }
    }



    /**
     * 统一解密入口：根据文件后缀解密，自动关闭原始流
     * @param inputStream 原始加密文件流（会被自动关闭）
     * @param fileSuffix 文件后缀（如 docx、xlsx，建议小写）
     * @param password 解密密码（非空且非空字符串）
     * @return 解密后的输入流（需调用方通过 FileUtils.read 或手动关闭）
     * @throws Exception 解密失败（密码错误、格式不支持、文件损坏）
     */
    public static InputStream decrypt(InputStream inputStream, String fileSuffix, String password) throws Exception {
        // 校验参数
        if (inputStream == null) {
            throw new IllegalArgumentException("原始文件流不能为空");
        }
        if (fileSuffix == null || fileSuffix.trim().isEmpty()) {
            throw new IllegalArgumentException("文件后缀不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("解密密码不能为空");
        }

        // 根据格式调用对应解密方法
        switch (fileSuffix) {
            case "doc":
            case "docx":
                return decryptWord(inputStream, password);
            case "xls":
            case "xlsx":
                return decryptExcel(inputStream,fileSuffix, password);
            case "ppt":
            case "pptx":
                return decryptPpt(inputStream, password);
            case "pdf":
                return decryptPdf(inputStream, password);
            default:
                throw new UnsupportedOperationException("不支持的解密格式：" + fileSuffix);
        }
    }


    /**
     * 解密 Word 文件（doc/docx）
     */
    private static InputStream decryptWord(InputStream inputStream, String password) throws IOException {
        Document doc = null;
        ByteArrayOutputStream baos = null;
        try {
            doc = new Document();
            // 加载加密Word（自动解密）
            doc.loadFromStream(inputStream, com.spire.doc.FileFormat.Auto, password);
            doc.unprotect();
            doc.removeEncryption();
            // 解密后的内容写入内存流
            baos = new ByteArrayOutputStream();
            doc.saveToStream(baos, com.spire.doc.FileFormat.Auto); // 保持原格式
            return new ByteArrayInputStream(baos.toByteArray());
        } finally {
            // 关键：关闭原始输入流（避免资源泄漏）
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
            // 释放 Spire 资源
            if (baos != null) {
                baos.close();
            }
            if (doc != null) {
                doc.dispose();
            }
        }
    }

    /**
     * 解密 Excel 文件（xls/xlsx）
     */
    private static InputStream decryptExcel(InputStream inputStream,String ext ,String password) throws IOException {
        Workbook workbook = null;
        ByteArrayOutputStream baos = null;
        try {
            workbook = new Workbook();
            // 加载加密Excel（自动解密）
            workbook.setOpenPassword(password);
            workbook.loadFromStream(inputStream);
            // 移除工作表保护（若有）
            workbook.unProtect();
            com.spire.xls.FileFormat saveFormat = "xls".equals(ext)
                    ? FileFormat.Version97to2003
                    : FileFormat.Version2007;
            // 解密后的内容写入内存流
            baos = new ByteArrayOutputStream();
            workbook.saveToStream(baos,saveFormat); // 自动识别原格式
            return new ByteArrayInputStream(baos.toByteArray());
        } finally {
            // 关键：关闭原始输入流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
            // 释放 Spire 资源
            if (baos != null) {
                baos.close();
            }
            if (workbook != null) {
                workbook.dispose();
            }
        }
    }

    /**
     * 解密 PPT 文件（ppt/pptx）
     */
    private static InputStream decryptPpt(InputStream inputStream, String password) throws Exception {
        Presentation ppt = null;
        ByteArrayOutputStream baos = null;
        try {
            ppt = new Presentation();
            // 加载加密PPT（自动解密）
            ppt.loadFromStream(inputStream, com.spire.presentation.FileFormat.PPT, password);
            // 移除文档加密
            ppt.removeEncryption();

            // 解密后的内容写入内存流
            baos = new ByteArrayOutputStream();
            ppt.saveToFile(baos, com.spire.presentation.FileFormat.PPT); // 保持原格式
            return new ByteArrayInputStream(baos.toByteArray());
        } finally {
            // 关键：关闭原始输入流
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("系统异常",e);
                    throw new RuntimeException(e);
                }
            }
            // 释放 Spire 资源
            if (baos != null) {
                baos.close();
            }
            if (ppt != null) {
                ppt.dispose();
            }
        }
    }

}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2023/7/12 Leo creat
 */
