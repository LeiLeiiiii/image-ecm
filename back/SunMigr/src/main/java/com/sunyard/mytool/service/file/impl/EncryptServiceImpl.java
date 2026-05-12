package com.sunyard.mytool.service.file.impl;

import cn.hutool.core.util.ObjectUtil;
import com.sunyard.mytool.dto.EncryptDTO;
import com.sunyard.mytool.dto.FileEncryptInfoDTO;
import com.sunyard.mytool.service.file.EncryptService;
import com.sunyard.mytool.until.Base64Utils;
import com.sunyard.mytool.until.FileEncryptUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * 文件加密实现类
 */
@Service
public class EncryptServiceImpl implements EncryptService {

    /**
     * 加密密钥
     */
    private static String ENCRYPT_KEY = "8a08826984224746";

    /**
     * 文件加密
     */
    public EncryptDTO encrypt(FileEncryptInfoDTO dto) {
        //如果密钥为空，则取配置
        String encryptKey = dto.getEncryptKey();
        if (ObjectUtil.isEmpty(encryptKey)) {
            encryptKey = ENCRYPT_KEY;
            //对密钥做base64加密
            encryptKey = Base64Utils.encodeBase64(encryptKey);
        }
        //加密类型
        Integer encryptType = dto.getEncryptType();
        return FileEncryptUtils.encrypt(dto.getInputStream(), encryptKey, encryptType);
    }

    /**
     * 文件解密
     */
    public InputStream decrypt(FileEncryptInfoDTO dto) {
        return FileEncryptUtils.decrypt(dto.getInputStream(), dto.getEncryptKey(),
                dto.getEncryptType(), dto.getEncryptLength());
    }
}
