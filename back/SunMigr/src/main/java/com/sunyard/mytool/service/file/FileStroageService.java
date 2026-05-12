package com.sunyard.mytool.service.file;


import com.sunyard.mytool.dto.UploadDTO;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.InputStream;

@Configuration
public interface FileStroageService {


    void fileUpload(String filePath, String bucketName, String key);

    void fileUpload(File file, String bucketName, String key);

    void upload(UploadDTO uploadDTO);

    InputStream getFileStream(String bucket, String key);

    long getFileSize(String bucket, String key);
}
