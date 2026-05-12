package com.sunyard.module.storage.service;

import java.io.InputStream;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.sunyard.framework.common.util.encryption.Base64Utils;
import com.sunyard.module.storage.config.properties.StorageUploadProperties;
import com.sunyard.module.storage.dto.EncryptDTO;
import com.sunyard.module.storage.dto.FileEncryptInfoDTO;
import com.sunyard.module.storage.util.FileEncryptUtils;

import cn.hutool.core.util.ObjectUtil;

/**
 * 文件加密实现类
 */
@Service
public class EncryptService {


    @Resource
    private StorageUploadProperties storageUploadProperties;
    /**
     * 文件加密
     */
    public EncryptDTO encrypt(FileEncryptInfoDTO dto) {
        //如果密钥为空，则取配置
        String encryptKey = dto.getEncryptKey();
        if (ObjectUtil.isEmpty(encryptKey)) {
            encryptKey = storageUploadProperties.getEncryptKey();
            //对密钥做base64加密
            encryptKey = Base64Utils.encodeBase64(encryptKey);
        }
        //加密类型
        Integer encryptType = dto.getEncryptType();
        return FileEncryptUtils.encrypt(dto.getInputStream(),
                encryptKey, encryptType,storageUploadProperties.getEncryptFileHome(),
                storageUploadProperties.getPartEncryptSize(),storageUploadProperties.getBytesMaxSize());
    }

    /**
     * 文件解密
     */
    public InputStream decrypt(FileEncryptInfoDTO dto) {
        return FileEncryptUtils.decrypt(dto.getInputStream(), dto.getEncryptKey(),
                dto.getEncryptType(), dto.getEncryptLength());
    }
}
