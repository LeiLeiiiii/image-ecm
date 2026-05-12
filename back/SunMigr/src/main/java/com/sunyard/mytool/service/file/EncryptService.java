package com.sunyard.mytool.service.file;



import com.sunyard.mytool.dto.EncryptDTO;
import com.sunyard.mytool.dto.FileEncryptInfoDTO;

import java.io.InputStream;

public interface EncryptService {


    /**
     * 文件加密
     */
    EncryptDTO encrypt(FileEncryptInfoDTO dto);


    /**
     * 文件解密
     */
    InputStream decrypt(FileEncryptInfoDTO dto);

}
