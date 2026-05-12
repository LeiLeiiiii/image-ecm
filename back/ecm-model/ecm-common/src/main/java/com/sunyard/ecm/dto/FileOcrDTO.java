package com.sunyard.ecm.dto;

import lombok.Data;

/**
 * 文本查重：文件信息类
 */
@Data
public class FileOcrDTO {

    /**
     * 文件id
     */
    private Long fileId;

    /**
     *  单据分类信息
     */
    private String fileType;

    /**
     *  全文识别内容
     */
    private String textAll;
}
