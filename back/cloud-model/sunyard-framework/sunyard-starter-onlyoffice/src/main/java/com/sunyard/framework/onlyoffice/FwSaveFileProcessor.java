package com.sunyard.framework.onlyoffice;

import java.util.Map;

/**
 * 保存文件处理执行的方法
 * @author PJW
 */
public interface FwSaveFileProcessor {
    /**
     * 保存文件前进行自定义处理
     * @param map map
     * @param bytes 文件byte
     * @param fileExtension 文件后缀
     * @throws Exception 异常
     */
     void saveBeforeInitialization(Map<String, Object> map,byte[] bytes,String fileExtension) throws Exception;

    /**
     * 保存文件
     * @param map     文件元信息
     * @param file    文件
     * @param changes 文件变动信息
     * @param key key
     */
     void save(Map<String, Object> map,byte[] file, byte[] changes,String key);

    /**
     * 保存文件后进行自定义处理
     * @param map map
     * @param bytes byte数组
     * @param fileExtension fileExtension
     * @throws Exception 异常
     */
     void saveAfterInitialization(Map<String, Object> map,byte[] bytes,String fileExtension) throws Exception;
}
