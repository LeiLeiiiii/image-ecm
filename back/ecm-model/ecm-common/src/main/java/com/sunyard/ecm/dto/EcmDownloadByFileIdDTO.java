package com.sunyard.ecm.dto;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author scm
 * @since 2023/8/21 16:55
 * @desc 文件下载DTO
 */
@Data
@ToString
@Accessors(chain = true)
public class EcmDownloadByFileIdDTO {


    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 文件名称
     */
    private String newFileName;

    /**
     * 文件后缀
     */
    private String format;

    /**
     * 资料节点
     */
    private String docCode;


    /**
     * 资料节点
     */
    private String docName;

    /**
     * 资源id
     */
    private Long newFileId;

    /**
     * 资源id
     */
    private String createUser;

}
