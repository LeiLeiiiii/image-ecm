package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 在线检测-图像查重结果返参
 * @author P-JWei
 * @date 2024/3/11 14:48:33
 * @title
 * @description
 */
@Data
public class AfmDetOnlineImgDetDTO implements Serializable {

    /**
     * 文件id（用于获取文件缩略图/原图）
     */
    private String fileFullPath;
    /**
     * 文件url
     */
    private String fileUrl;
    /**
     * 主键
     */
    private Long id;
    /**
     * 主键
     */
    private Long noteId;
    /**
     * 文件md5
     */
    private String fileIndex;
    /**
     * 主键
     */
    private Long exifId;
    /**
     * 文件id（用于获取文件缩略图/原图）
     */
    private Long fileId;
    /**
     * 文件md5
     */
    private String fileMd5;
    /**
     * 来源系统
     */
    private String sourceSys;
    /**
     * 文件类型
     */
    private String format;
    /**
     * 业务类型
     */
    private String businessType;

    /**
     * exif 信息
     */
    private Map fileExifMap;

    /**
     * 主索引
     */
    private String businessIndex;

    /**
     * 资料类型
     */
    private String materialType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 查重时间
     */
    private Date imgDupTime;

    /**
     * 相似度
     */
    private String similarity;

    /**
     * 文件元数据（json格式）
     */
    private String fileExif;
    /**
     * 文件大小
     */
    private String fileSize;
    /**
     * 上传机构
     */
    private String uploadOrg;

    /**
     * 上传人登录名
     */
    private String uploadUserCode;

    /**
     * 上传人（姓名）
     */
    private String uploadUserName;


    /**
     * 更新时间
     */
    private Date createTime;

    /**
     * 文本查重内容
     */
    private String fileText;

    private Integer type;
}
