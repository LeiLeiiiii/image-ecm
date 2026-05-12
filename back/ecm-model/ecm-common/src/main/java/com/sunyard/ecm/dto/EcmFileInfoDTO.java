package com.sunyard.ecm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 * @author： ty
 * @create： 2023/4/25 14:41
 * @desc：文件信息DTO
 */
@Data
public class EcmFileInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * value = "文件id
     **/
    private Long fileId;

    /**
     * value = "业务表主键
     **/
    private Long busiId;

    /**
     * value = "资料树主键
     **/
    private String docId;

    /**
     * value = "资料树标记主键
     **/
    private Long markDocId;

    /**
     * value = "最新的文件id
     **/
    private Long newFileId;

    /**
     * value = "锁定状态（0:未锁定、1:锁定（移动））
     **/
    private Integer newFileLock;

    /**
     * value = "文件名称
     **/
    private String newFileName;

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
     * value = "是否复用（默认0，1:复用）
     **/
    private Integer fileReuse;

    /**
     * value = "顺序（在doc_id下排序）
     **/
    private Double fileSort;

    /**
     * value = "创建人
     **/
    private String createUser;

    /**
     * value = "创建时间
     **/
    private Date createTime;

    /**
     * value = "最新修改人
     **/
    private String updateUser;

    /**
     * value = "最新修改时间
     **/
    private Date updateTime;

    /**
     * value = "状态(默认正常展示:0,已删除:1)
     **/
    private Integer state;

    /**
     * value = "文件大小
     **/
    private String size;
    /**
     * value = "文件格式
     **/
    private String format;
    /**
     * value = "创建人名称
     **/
    private String createUserName;
    /**
     * value = "最新修改人名称
     **/
    private String updateUserName;
    /**
     * value = "资料权限版本
     **/
    private Integer rightVer;
    /**
     * value = "资料类型名称
     **/
    private String docName;
    /**
     * value = "业务批次号
     **/
    private String busiBatchNo;
    /**
     * value = "new标记: 0无，1有
     **/
    private Integer newTag;
    /**
     * value = "旋转前文件id
     **/
    private Long oldFileId;
    /**
     * value = "资料类型id
     **/
    private String docCode;
    /**
     * value = "文件EXIF
     **/
    private HashMap fileExif;
    /**
     * value = "sourceFileMd5
     **/
    private String sourceFileMd5;

    /**
     * value = "主键
     **/
    private Long id;

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

    /**
     * value = "文件扩展名
     **/
    private String newFileExt;

    /**
     * value = "上传Id（每个文件有唯一的一个上传id）
     **/
    private String uploadId;

    /**
     * value = "所属桶名
     **/
    private String bucketName;

    /**
     * value = "文件的key(桶下的文件路径)
     **/
    private String objectKey;

    /**
     * value = "每个分片大小（byte）
     **/
    private Long chunkSize;

    /**
     * value = "分片数量
     **/
    private Integer chunkNum;

    /**
     * value = "文件来源
     **/
    private String fileSource;

    /**
     * value = "文件访问地址(local)
     **/
    private String url;

    /**
     * value = "文件相对路径
     **/
    private String filePath;

    /**
     * value = "存储平台
     **/
    private String platform;

    /**
     * value = "基础存储路径（文件所在位置）
     **/
    private String basePath;

    /**
     * value = "缩略图路径
     **/
    private String thUrl;

    /**
     * value = "缩略图名称
     **/
    private String thFilename;

    /**
     * value = "缩略图大小
     **/
    private Double thSize;

    /**
     * value = "删除状态(否:0,是:1)
     **/
    private Integer isDeleted;

    /**
     * value = "是否上传完成（0:未完成，1：完成）
     **/
    private Integer isUploadOk;

    /**
     * value = "备注
     **/
    private String comment;

    /**
     * value = "业务类型id
     **/
    private String appCode;

    /**
     * value = "业务类型名称
     **/
    private String appTypeName;

    /**
     * value = "业务编号
     **/
    private String busiNo;

    /**
     * value = "文件new标签标识(1-有new标签 ，0-没有new标签)
     **/
    private Integer signFlag;

    /**
     * value = "机构号
     **/
    private String orgCode;

    /**
     * value = "机构名称
     **/
    private String orgName;

    /**
     * 资料文件顺序
     **/
    private String docFileSort;

    /**
     * 存储设备id
     **/
    private Long equipmentId;

    /**
     * 树类型(0:静态树 1:动态树)
     **/
    private String treeType;

    /**
     * 是否加密0否 1是
     */
    private Integer isEncrypt;

    /**
     * 文档是否有密码
     */
    private Boolean isFilePassword;

}
