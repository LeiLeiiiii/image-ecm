package com.sunyard.mytool.service.file.impl;


import cn.hutool.core.io.FileUtil;
import com.sunyard.mytool.dto.UploadDTO;
import com.sunyard.mytool.entity.StEquipment;
import com.sunyard.mytool.service.file.FileStroageService;


import java.io.File;
import java.io.InputStream;

public class LocalFileStroageServiceImpl implements FileStroageService {


    public LocalFileStroageServiceImpl(StEquipment type) {
    }

    @Override
    public void fileUpload(String filePath, String bucketName, String key) {
        
    }

    @Override
    public void fileUpload(File file, String bucketName, String key) {

    }

    @Override
    public void upload(UploadDTO uploadDTO) {
        String url = uploadDTO.getBasePath() + File.separator + uploadDTO.getKey();
        FileUtil.writeFromStream(uploadDTO.getInputStream(), url);
    }

    @Override
    public InputStream getFileStream(String bucket, String key) {
        return null;
    }

    @Override
    public long getFileSize(String bucket, String key) {
        return 0;
    }
}
