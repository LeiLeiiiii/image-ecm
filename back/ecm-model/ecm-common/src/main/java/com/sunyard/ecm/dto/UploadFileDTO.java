package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author scm
 * @since 2023/8/24 13:45
 * @desc 文件上传信息DTO
 */
@Data
public class UploadFileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 上传文件
     */
    private List<FileAndSortDTO> fileAndSortDTOS;

    /**
     * 文件及其md5
     */
    private Map<String, FileAndSortDTO> md5s;


    /**
     * 解析后的文件md5
     */
    private Set<String> md5List;

    /**
     * 资料节点
     */
    private String docNo;

}
