package com.sunyard.module.storage.dto;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 文件表
 * </p>
 *
 * @author panjiazhu
 * @since 2022-07-12
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class StFileDTO implements Serializable {

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
     * 存储设备id
     */
    private Long equipmentId;

    /**
     * 源文件MD5
     */
    private String sourceFileMd5;

    /**
     * 目标文件MD5
     */
    private String fileMd5;

    /**
     * 文件文档密码
     */
    private String password;














    /**
     * 上传人
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private Date createTime;



    /**
     * 删除状态(否:0,是:1
     */
    @TableLogic
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
     * 加密算法,0为AES,1为SM2
     */
    private Integer encryptType;

    /**
     * 加密的密文长度
     */
    private Long encryptLen;

}
