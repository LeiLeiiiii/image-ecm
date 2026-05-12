package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


/**
 * 迁移批次表实体
 */
@Data
@TableName("B_File_TEMP")
public class FileTemp {

    /**
     * 主键(用客户文件表主键,存ecm_file_info.page_id)
     */
    @TableId(value = "FILE_ID")
    private String fileId;

    /**
     * 业务类型code
     */
    @TableField(value = "APP_CODE")
    private String appCode;

    /**
     * 业务号
     */
    @TableField(value = "BUSI_NO")
    private String busiNo;

    /**
     * 文件名
     */
    @TableField(value = "FILE_NAME")
    private String fileName;

    /**
     * 文件扩展名
     */
    @TableField(value = "FILE_EXT")
    private String fileExt;

    /**
     * 文件大小
     */
    @TableField(value = "FILE_SIZE")
    private Long fileSize;

    /**
     * 文件的key(桶下的文件路径)
     */
    @TableField(value = "FILE_PATH")
    private String filePath;

    /**
     * 源文件MD5
     */
    @TableField(value = "sourcefile_md5")
    private String sourceFileMD5;

    /**
     * 目标文件MD5
     */
    @TableField(value = "FILE_MD5")
    private String fileMd5;

    /**
     * 顺序（在doc_id下排序）
     */
    @TableField(value = "FILE_SORT")
    private Double fileSort;

    /**
     * 文件扩展信息
     */
    @TableField(value = "FILE_EXIF")
    private String fileExif;

    /**
     * 是否加密
     */
    @TableField(value = "IS_ENCRYPT")
    private Integer isEncrypt;

    /**
     * 上传人
     */
    @TableField(value = "UP_USER")
    private String upUser;

    /**
     * 上传人姓名
     */
    @TableField(value = "UP_NAME")
    private String upName;

    /**
     * 上传时间
     */
    @TableField(value = "UP_TIME")
    private Date upTime;

    /**
     * 修改人
     */
    @TableField(value = "MOD_USER")
    private String modUser;

    /**
     * 修改人姓名
     */
    @TableField(value = "MOD_NAME")
    private String modName;

    /**
     * 修改时间
     */
    @TableField(value = "MOD_TIME")
    private Date modTime;

    /**
     * 资料树主键
     */
    @TableField(value = "DOC_CODE")
    private String docCode;

    /**
     * 资料树名称
     */
    @TableField(value = "DOC_NAME")
    private String docName;

    /**
     * 标签名称
     */
    @TableField(value = "LABEL_NAME")
    private String labelName;

    /**
     * 标记名称
     */
    @TableField(value = "MARK_NAME")
    private String markName;

    /**
     * 标记顺序
     */
    @TableField(value = "MARK_SORT")
    private Float markSort;

    /**
     * 是否历史文件 0:否,1:是
     */
    @TableField(value = "IS_HISTORY")
    private String isHistory;

    /**
     * 版本号
     */
    @TableField(value = "FILE_VERSION")
    private String fileVersion;

    /**
     * 源文件ID
     */
    @TableField(value = "SOURCE_FILE_ID")
    private String sourceFileId;

    /**
     * 迁移状态 0:待迁移,1:迁移中,2:迁移成功,-1:迁移失败
     */
    @TableField(value = "MIG_STATUS")
    private Integer migStatus;

    /**
     * 迁移完成时间
     */
    @TableField(value = "MIG_TIME")
    private Date migTime;

    /**
     * 迁移失败原因
     */
    @TableField(value = "FAIL_REASON")
    private String failReason;


    /**
     * 存储类型
     */
    @TableField(value = "CLOUD_TYPE")
    private Integer cloudType;


    @TableField(value = "BUCKET")
    private String bucket;



}
