package com.sunyard.module.storage.config.factory;

/**
 * 存储平台的 Client 的对象的工厂接口
 * @author PJW
 */
public interface FileStorageClientFactory<Client> extends AutoCloseable {

    /**
     * 获取平台
     * @return Result
     */
    String getPlatform();

    /**
     * 获取 Client ，部分存储平台例如 FTP 、 SFTP 使用完后需要归还
     * @return Result
     */
    Client getClient();

    /**
     * 释放相关资源
     */
    @Override
    default void close() {}

}
