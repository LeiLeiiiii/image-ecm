package com.sunyard.ecm.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * @author scm
 * @since 2023/8/24 13:45
 * @desc 文件排序DTO
 */
@Data
public class FileAndSortDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 顺序
     */
    private Long fileSort;

    /**
     * 上传文件
     */
    private MultipartFile multipartFile;
    /**
     * 文件标签
     */
    private List<String> fileLabels;
    /**
     * 目标文件md5
     */
    private String fileMd5;

    /**
     * 源文件md5
     */
    private String sourceFileMd5;

}
