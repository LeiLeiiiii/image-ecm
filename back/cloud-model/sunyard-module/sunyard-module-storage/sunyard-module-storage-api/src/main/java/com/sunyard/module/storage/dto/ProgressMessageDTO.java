package com.sunyard.module.storage.dto;

import lombok.Data;

/**
 * @author zyl
 * @Description
 * @since 2023/11/27 14:39
 */
@Data
public class ProgressMessageDTO {
    /**
     * 文件名称
     */
    private String newFileName;
    /**
     * 文件md5
     */
    private String md5;
    /**
     * 上传进度
     */
    private String progress;
    /**
     * 文件id
     */
    private Long fileId;
}
