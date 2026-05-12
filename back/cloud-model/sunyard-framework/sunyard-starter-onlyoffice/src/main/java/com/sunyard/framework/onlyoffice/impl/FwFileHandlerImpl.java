package com.sunyard.framework.onlyoffice.impl;

import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.sunyard.framework.onlyoffice.FwFileHandler;
import com.sunyard.framework.onlyoffice.constant.StateConstants;
import com.sunyard.framework.onlyoffice.core.Cache;
import com.sunyard.framework.onlyoffice.dto.FwFileMetaData;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 文件处理方式
 *
 * getID_+key                   存储的文件id
 * collaborativeEditing_+文件id  存储的 key  协同编辑使用同一个 key
 * userID+文件id                 存储的 key
 * @author PJW
 */
@Component
@Slf4j
public class FwFileHandlerImpl implements FwFileHandler {

    @Resource
    private Cache cache;

    private int time = 60*60*60;



    @Override
    public String getHandlerName() {
        return "onlyOffice-";
    }


    /**
     * @param map
     * @param collaborativeEditing
     * @return Result
     * @throws Exception
     */
    @Override
    public FwFileMetaData handlerFile(Map<String, Object> map, boolean collaborativeEditing) throws Exception {

        try {
            String id = (String) map.get("fileId");
            log.info("源文件存储的ID" + id);
            // 唯一标示符
            /**
             *  协同编辑时用的同一个key，判断是否协同，是：查找缓存中有无key，否：新生成key
             * */
            String key;
            String mode = (String) map.get("mode");
            //编辑模式
            if (StateConstants.EDIT.equals(mode)) {
                if (collaborativeEditing) {
                    //协同模式
                    if (cache.get(StateConstants.REDISEDIT + id) != null) {
                        key = (String) cache.get(StateConstants.REDISEDIT + id);
                    } else {
                        key = IdUtil.simpleUUID();
                        cache.set(StateConstants.REDISEDIT + id, key, time);
                        cache.set(StateConstants.REDISGETID + key, id, time);
                    }
                } else {
                    //非协同模式
                    key = IdUtil.simpleUUID();
                    String userId = map.get("userId").toString();
                    cache.set(userId + "_" + id, key, time);
                    cache.set(StateConstants.REDISGETID + key, id, time);
                }
            } else {//查看模式
                key = id;
            }
            Optional<FwFileMetaData> tempFileInfoOptional = this.getTempFile(key);
            if (tempFileInfoOptional.isPresent()) {
                return tempFileInfoOptional.get();
            }
            //  生成临时可访问文件的url
          //  String tempUrl = getFileUri(id);
            String tempUrl = map.get("fileUrl").toString();
            FwFileMetaData tempFileInfo = FwFileMetaData.builder()
                    // 可访问路径
                    .url(tempUrl)
                    // 原来的文件名
                    .oldName((String) map.get("fileName"))
                    .fileType((String) map.get("fileType"))
                    .fileInfo(map)
                    // 唯一标识
                    .key(key)
                    .build();
            if (mode.equals(StateConstants.EDIT)) {
                FwFileContextImpl.KEY_URL_INFO.put(getHandlerName() + key, tempFileInfo);
            }
            return tempFileInfo;
        } catch (Exception e) {
            log.error("生成临时文件失败", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public void removeTempFile(String key) {
        Optional<FwFileMetaData> tempFileInfo = this.getTempFile(key);
        if (tempFileInfo.isPresent()) {
            FwFileContextImpl.KEY_URL_INFO.remove(getHandlerName() + key);
        }
    }


    /**
     * 判断是否存在临时文件信息
     *
     * @param key
     * @return Result
     */
    @Override
    public Optional<FwFileMetaData> getTempFile(String key) {
        FwFileMetaData tempFileInfo = FwFileContextImpl.KEY_URL_INFO.get(getHandlerName() + key);
        return Optional.ofNullable(tempFileInfo);
    }
}
