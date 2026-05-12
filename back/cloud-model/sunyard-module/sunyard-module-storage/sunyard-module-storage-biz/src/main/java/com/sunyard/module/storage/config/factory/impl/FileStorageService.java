package com.sunyard.module.storage.config.factory.impl;

import cn.hutool.core.util.StrUtil;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.module.storage.config.factory.FileStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author zyl
 * @Description
 * @since 2024/3/19 9:35
 */
@Slf4j
@Getter
@Setter
public class FileStorageService {

    private FileStorageService self;

    private CopyOnWriteArrayList<FileStorage> fileStorageList;

    /**
     * 获取对应的存储平台
     *
     * @param platform 平台
     * @param <T>      T
     * @return FileStorage
     */
    public <T extends FileStorage> T getFileStorage(String platform) {
        for (FileStorage fileStorage : fileStorageList) {
            if (fileStorage.getPlatform().equals(platform)) {
                return cast(fileStorage);
            }
        }

        return null;
    }

    /**
     * 获取对应的存储平台，如果存储平台不存在则抛出异常
     *
     * @param platform 平台
     * @param <T>      T
     * @return FileStorage
     */
    public <T extends FileStorage> T getFileStorageVerify(String platform) {
        T fileStorage = self.getFileStorage(platform);
        if (fileStorage == null) {
            throw new SunyardException(StrUtil.format("没有找到对应的存储平台！platform:{}", platform));
        }

        return fileStorage;
    }

    private static <T> T cast(Object object) {
        return (T) object;
    }
}
