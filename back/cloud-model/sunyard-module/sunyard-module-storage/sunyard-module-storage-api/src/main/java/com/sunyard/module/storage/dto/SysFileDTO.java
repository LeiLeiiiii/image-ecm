package com.sunyard.module.storage.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author： zyl
 * @Description：
 * @create： 2023/5/23 16:30
 */
@Data
public class SysFileDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    private Long id;

    /**
     * 文件原始名称
     */
    private String originalFilename;

    /**
     * 文件名  / 银行影像 contentId  /查询时 batchId-id
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
     * 配置编号
     */
    private Long configId;

    /**
     * 上传人
     */
    private Long createUser;

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

    /**
     * 是否加密 （0否 1是）
     */
    private Integer isEncrypt;

    /**
     * 加密密钥
     */
    private String encryptKey;

    /**
     * 加密标识符
     */
    private String encryptIndex;

    /**
     * 存储设备id
     */
    private Long equipmentId;

    /**
     * 文档类型第一页的文本内容
     */
    private String contentFirstPage;

    /**
     * 文件密码
     */
    private Boolean isFilePassword;
}
