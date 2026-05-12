package com.sunyard.module.storage.config.properties;
/*
 * Project: Sunyard
 *
 * File Created at 2025/9/21
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author Leo
 * @Desc
 * @date 2025/9/21 20:13
 */
@Data
@RefreshScope
@Component
public class StorageUploadProperties {
    @Value("${upload.python-dir:false}")
    private String pythonDir;
    @Value("${upload.temp-cache:true}")
    private Boolean cacheEnable;
    @Value("${upload.chunk-size}")
    private Long chunkSize;
    /**
     * ======================================代理相关====================================
     */
    @Value("${upload.lan-proxy-ip}")
    private String lanProxyIp;
    @Value("${upload.lan-proxy-port}")
    private String lanProxyPort;
    @Value("${upload.wan-proxy-ip}")
    private String wanProxyIp;
    @Value("${upload.wan-proxy-port}")
    private String wanProxyPort;

    /**
     * =====================================sftp=======================================
     */
    @Value("${customize.remoteServer.sftp.SFTP_host:172.1.3.82}")
    private String sftpHost;
    @Value("${customize.remoteServer.sftp.SFTP_enable:false}")
    private Boolean sftpEnable;

    /**
     * =======================================文件路径相关========================================
     */
    //临时目录
    @Value("${upload.temp-dir:/home/temp/}")
    private String sftpDirectory;
    //下载临时路径
    @Value("${upload.file-down-temp:/home/temp/storage/fileDown}")
    private String fileDownTemp;
    //加密路径
    @Value("${upload.file_home:/home/temp/storage/encrypt/}")
    private  String encryptFileHome;
    // 分片文件临时存储文件夹
    @Value("${upload.temporary-folder:/home/temp/storage/temporaryFolder}")
    private String temporaryFolder;
    //pdf拆分临时目录
    @Value("${upload.pdf_split_temp-dir:/home/temp/pdfSplit/}")
    private String pdfSplitDirectory;
    /**
     * =======================================加密相关===========================================
     */
    /**
     * 存储路径key类型,默认date日期,hash则为hash存储
     */
    @Value("${upload.key-type:date}")
    private String keyType;

    /**
     * 加密密钥
     */
    @Value("${upload.encrypt.encrypt-key:8a08826984224746}")
    private String encryptKey;

    /**
     * 加密最大范围： 单位KB
     */
    @Value("${upload.encrypt.part-encrypt-size-local:800}")
    private Integer partEncryptSizeForLocal = 800;

    /**
     * aes加密标识符（用于区分加密、未加密数据）
     */
    @Value("${upload.encrypt.encrypt-index:<ENCRYPTED>}")
    private String encryptIndex = "<ENCRYPTED>";
    /**
     * #国密（SM2）加密标识符（用于区分加密部分、未加密部分）
     */
    @Value("${upload.encrypt.sm2-encrypt-index:<SM2_ENCRYPTED>}")
    private String sm2EncryptIndex = "<SM2_ENCRYPTED>";

    /**
     * 字节加密最大范围：单位MB
     */
    @Value("${upload.encrypt.bytes-max-size:200}")
    private  Integer bytesMaxSize;
    /**
     * 加密最大范围： 单位KB
     */
    @Value("${upload.encrypt.part-encrypt-size:2}")
    private  Integer partEncryptSize;

}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2025/9/21 Leo creat
 */
