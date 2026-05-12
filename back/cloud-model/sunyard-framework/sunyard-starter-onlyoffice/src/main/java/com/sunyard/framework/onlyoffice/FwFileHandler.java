package com.sunyard.framework.onlyoffice;


import java.util.Map;
import java.util.Optional;

import com.sunyard.framework.onlyoffice.dto.FwFileMetaData;


/**
 * @author 朱山成
 */
public interface FwFileHandler {
    /**
     * 处理名称
     *
     * @return Result 名称
     */
    String getHandlerName();


    /**
     * 处理文件
     *
     * @param map 必填 fileId 必填 fileName 必填 fileType 必填 fileSize 可用携带其它值
     * @param collaborativeEditing 是否修改
     * @return Result 文件元数据对象
     * @throws Exception 异常
     */
    FwFileMetaData handlerFile(Map<String, Object> map, boolean collaborativeEditing) throws Exception;

    /**
     * 移除临时文件信息
     *
     * @param key key
     */
    void removeTempFile(String key);

    /**
     * 获取临时文件信息
     * @param key key
     * @return Result 文件元数据对象
     */
     Optional<FwFileMetaData> getTempFile(String key);
}
