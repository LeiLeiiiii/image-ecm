package com.sunyard.module.storage.config.factory.impl;

import com.jcraft.jsch.ChannelSftp;
import com.sunyard.framework.common.util.FtpUtils;
import com.sunyard.module.storage.config.factory.FileStorageClientFactory;
import com.sunyard.module.storage.po.StEquipment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author zyl
 * @Description
 * @since 2024/4/29 14:58
 */
@Getter
@Setter
@NoArgsConstructor
public class SftpFileStorageClientFactory implements FileStorageClientFactory<ChannelSftp> {

    private String platform;
    private String accessKey;
    private String secretKey;
    private String domain;
    private String basePath;
    private String endPoint;
    private String bucketName;
    private volatile ChannelSftp client;

    public SftpFileStorageClientFactory(StEquipment config){
        platform = String.valueOf(config.getId());
        accessKey = config.getAccessKey();
        secretKey = config.getAccessSecret();
        domain = config.getDomainName();
        endPoint = config.getStorageAddress();
        basePath = config.getBasePath();
    }

    @Override
    public ChannelSftp getClient() {
        if (client==null){
            synchronized (this) {
                ChannelSftp channelSftp = null;
                if (client == null) {
                    try {
                        channelSftp = FtpUtils.getConnect(domain.split(":")[0], Integer.parseInt(domain.split(":")[1]), accessKey, secretKey);
                    } catch (Exception e) {
                        
                    }
                    client = channelSftp;
                }
            }
        }
        return client;
    }

}
