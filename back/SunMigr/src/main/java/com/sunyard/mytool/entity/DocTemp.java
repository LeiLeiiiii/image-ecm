package com.sunyard.mytool.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文档迁移中间表实体类
 */
@TableName("doc_temp")
@Data
public class DocTemp {

    /**
     * 主键（原Pk_Id）
     */
    @TableId(value = "pk_id")
    private String pkId;

    /**
     * 文件完整路径
     */
    @TableField(value = "tx_path")
    private String txPath;

    /**
     * 文件名称
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 文件后缀
     */
    @TableField(value = "file_ext")
    private String fileExt;

    /**
     * 文件MD5值
     */
    @TableField(value = "sourcefile_md5")
    private String sourceFileMD5;

    /**
     * 文件MD5值
     */
    @TableField(value = "file_md5")
    private String fileMD5;

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
     * 创建人
     */
    @TableField(value = "CREATE_USER")
    private String createUser;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME")
    private Date modifyTime;

    /**
     * 迁移时间
     */
    @TableField(value = "mig_time")
    private Date migTime;


    /**
     * 迁移状态: 0:待迁移,1:迁移中,2迁移成功,-1:迁移失败
     */
    @TableField(value = "mig_status")
    private Integer migStatus;

    /**
     * 迁移失败原因
     */
    @TableField(value = "fail_reason")
    private String failReason;
}
