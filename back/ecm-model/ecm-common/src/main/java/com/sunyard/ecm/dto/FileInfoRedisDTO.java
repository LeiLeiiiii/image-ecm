package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author scm
 * @since 2023/8/24 13:45
 * @desc 文件缓存信息DTO
 */
@Data
public class FileInfoRedisDTO extends EcmFileInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件EXIF
     **/
    private HashMap fileExif;


    /**
     * 文件批注数量
     **/
    private Integer fileCommentCount;

    /**
     * 文件全路径
     **/
    private String fileFullPath;

    /**
     * 缓存中的地址
     **/
    private String fileFullPathCache;

    /**
     * 缓存中的缩略图地址
     **/
    private String fileFullPathCacheThumbnail;

    /**
     * 计算后文件大小
     **/
    private String fileSize;

    /**
     * 文件单位
     **/
    private String fileUnit;

}
