package com.sunyard.ecm.util;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 该类是 Spring {@link MultipartFile} 接口的实现。
 * 它允许将文件上传处理为字节数组，提供了一种方便的方法
 * 从内存中的字节数据创建 MultipartFile 实例。
 * 该类提供访问文件元数据（名称、原始文件名、内容类型）的方法，
 * 以及实际文件内容。它在需要处理不在物理磁盘上
 * 但存储在内存中的文件时非常有用，例如通过网络表单上传的文件。
 */
public class ByteArrayMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public ByteArrayMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            java.nio.file.Files.copy(inputStream, dest.toPath());
        }
    }
}