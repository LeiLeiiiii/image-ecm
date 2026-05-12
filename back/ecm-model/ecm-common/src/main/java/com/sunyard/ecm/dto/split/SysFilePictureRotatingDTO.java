package com.sunyard.ecm.dto.split;

import lombok.Data;

import java.util.Date;

/**
 * @author： zyl
 * @create： 2023/6/12 15:14
 * @Description：图片编辑DTO
 */
@Data
public class SysFilePictureRotatingDTO {

    /**
     * 旋转前文件id
     */
    private Long oldFileId;
    /**
     * 主键
     */
    private Long id;

    /**
     * 文件原始名称
     */
    private String originalFilename;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件扩展名
     */
    private String ext;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 上传Id（每个文件有唯一的一个上传id）
     */
    private String uploadId;

    /**
     * 所属桶名
     */
    private String bucketName;

    /**
     * 文件的key(桶下的文件路径)
     */
    private String objectKey;

    /**
     * 每个分片大小（byte）
     */
    private Long chunkSize;

    /**
     * 分片数量
     */
    private Integer chunkNum;

    /**
     * 文件来源
     */
    private String fileSource;

    /**
     * 文件访问地址(local)
     */
    private String url;

    /**
     * 文件相对路径
     */
    private String filePath;

    /**
     * 源文件MD5
     */
    private String sourceFileMd5;

    /**
     * 目标文件MD5
     */
    private String fileMd5;

    /**
     * 存储平台
     */
    private String platform;

    /**
     * 基础存储路径（文件所在位置）
     */
    private String basePath;

    /**
     * 缩略图路径
     */
    private String thUrl;

    /**
     * 缩略图名称
     */
    private String thFilename;

    /**
     * 缩略图大小
     */
    private Double thSize;

    /**
     * 业务批次号
     */
    private String busiBatchNo;

    /**
     * 上传人
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1
     */
    private Integer isDeleted;

    /**
     * 是否上传完成（0:未完成，1：完成）
     */
    private Integer isUploadOk;
}
