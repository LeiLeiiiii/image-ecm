package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

import java.util.List;

/**
 * @author yzy
 * @desc
 * @since 2025/12/1
 */
@Data
public class DocFileZip {

    /**
     * 压缩包二级目录名
     */
    private String dirsSecond;

    /**
     * 文件ID
     */
    private List<Long> fileIds;
}
