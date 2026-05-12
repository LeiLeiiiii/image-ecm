package com.sunyard.framework.onlyoffice.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.sunyard.framework.onlyoffice.FwFileHandler;
import com.sunyard.framework.onlyoffice.dto.FwFileMetaData;

/**
 * 存储文件信息
 * @author PJW
 */
@Component
@Configuration
public class FwFileContextImpl {

    public final Map<String, FwFileHandler> tempMaps = new ConcurrentHashMap<>();

    public static final Map<String, FwFileMetaData> KEY_URL_INFO = new HashMap<>();

    @Autowired
    public FwFileContextImpl(Map<String, FwFileHandler> tempMap) {
        this.tempMaps.clear();
        tempMap.forEach((k, v) -> this.tempMaps.put(v.getHandlerName(), v));
    }

    /**
     * 获取对应的执行
     *
     * @param handler
     * @return Result 结果
     */
    public FwFileHandler getHandler(String handler) {
        return tempMaps.get(handler);
    }

    /**
     * 获取对应的 处理方式
     *
     * @param key key
     * @return Result 结果
     */
    public FwFileHandler getHandlerByKey(String key) {
        for (FwFileHandler tempFwFileHandler : tempMaps.values()) {
            FwFileMetaData tempFileInfo = KEY_URL_INFO.get(tempFwFileHandler.getHandlerName() + key);
            if (Objects.nonNull(tempFileInfo)) {
                return tempFwFileHandler;
            }
        }
        return null;
    }

    public FwFileMetaData getFileInfo(String key) {
        for (FwFileHandler fwFileHandler : tempMaps.values()) {
            FwFileMetaData fileInfo = KEY_URL_INFO.get(fwFileHandler.getHandlerName() + key);
            if (Objects.nonNull(fileInfo)) {
                return fileInfo;
            }
        }
        return null;
    }

}
