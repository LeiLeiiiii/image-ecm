package com.sunyard.sunafm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author pjw
 * @since 2024-09-30
 */
@Data
@TableName("afm_file_exif")
public class AfmFileExif implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "exif_id", type = IdType.ASSIGN_ID)
    private Long exifId;

    /**
     * 文件主索引
     */
    private String fileIndex;

    /**
     * 文件md5
     */
    private String fileMd5;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件url
     */
    private String fileUrl;

    /**
     * 来源系统
     */
    private String sourceSys;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务索引（主索引）
     */
    private String businessIndex;

    /**
     * 资料类型
     */
    private String materialType;

    /**
     * 上传人登录名
     */
    private String uploadUserCode;

    /**
     * 上传人（姓名）
     */
    private String uploadUserName;

    /**
     * 上传机构
     */
    private String uploadOrg;

    /**
     * 文件元数据（json格式）
     */
    private String fileExif;

    /**
     * 是否已存入向量数据库
     */
    private Integer isVector;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除（0否 1是）
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 关联的服务器id
     */
    private Long serverId;

    /**
     * 文件查重类型  0：影像查重  1：文本查重
     */
    private Integer type;

    /**
     * 文本查重： 文本内容
     */
    private String fileText;
}
