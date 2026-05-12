package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author： ty
 * @create： 2023/4/25 14:41
 * @desc: 对外接口文件信息DTO
 */
@Data
public class EcmFileInfoOpenDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * value = "文件id
     **/
    private Long fileId;

    /**
     * value = "资料树主键
     **/
    private String docId;

    /**
     * value = "文件地址
     **/
    private String newFileUrl;

    /**
     * value = "缩略图地址
     **/
    private String thumFileUrl;

    /**
     * value = "文件唯一md5（可查重使用）
     **/
    private String fileMd5;

    /**
     * value = "文件大小
     **/
    private String size;

    /**
     * value = "文件原始名称
     **/
    private String originalFilename;

    /**
     * value = "文件名
     **/
    private String filename;

    /**
     * value = "文件扩展名
     **/
    private String ext;

}
