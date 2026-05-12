package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.util.Map;


/**
 * 文件元数据信息
 * @author PJW
 */
@Data
public class FileMetaDTO {
    /**
     * 文件可访问的url  onlyoffice 下载文件的地址
     */
    private String url;
    /**
     * 文件标示符 最大长度为10位
     */
    private String key;
    /**
     * 文件名称  源文件名
     */
    private String oldName;

    private String fileType;


    /**
     * 历史版本路径
     */
    private String histVerPath;


    /**
     * 文件信息
     */
    private Map<String,Object> fileInfo;


}
