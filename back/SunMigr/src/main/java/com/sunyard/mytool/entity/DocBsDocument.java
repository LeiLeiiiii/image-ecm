package com.sunyard.mytool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文档业务文档实体类
 */
@TableName("doc_bs_document")
@Data
public class DocBsDocument {

    /**
     * 主键id
     */
    @TableId(value = "bus_id", type = IdType.NONE)
    private Long busId;

    /**
     * 所属文档库id
     */
    @TableField(value = "house_id")
    private Long houseId;

    /**
     * 顺序号
     */
    @TableField(value = "doc_seq")
    private Long docSeq;

    /**
     * 类型：0:文件夹；1:文档；2:附件
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 层级
     */
    @TableField(value = "folder_level")
    private Integer folderLevel;

    /**
     * 直接父级id
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 文件夹or文档名称
     */
    @TableField(value = "doc_name")
    private String docName;

    /**
     * 文件or文档描述
     */
    @TableField(value = "doc_describe")
    private String docDescribe;

    /**
     * 附件关联文档id
     */
    @TableField(value = "rel_doc")
    private Long relDoc;

    /**
     * 文件后缀，例子：.doc/.pdf
     */
    @TableField(value = "doc_suffix")
    private String docSuffix;

    /**
     * 文件夹或者文档的大小
     */
    @TableField(value = "doc_size")
    private Long docSize;

    /**
     * 主文件url
     */
//    @TableField(value = "doc_url")
//    private String docUrl;

    /**
     * 主文件上传的文件id
     */
    @TableField(value = "file_id")
    private Long fileId;

    /**
     * 文档状态，0:未上架，1:待上架 2:已上架 3:已下架
     */
    @TableField(value = "doc_status")
    private Integer docStatus;

    /**
     * 类型：0:企业，1:个人
     */
    @TableField(value = "doc_type")
    private Integer docType;

    /**
     * 所有者
     */
    @TableField(value = "doc_owner")
    private Long docOwner;

    /**
     * 创建人
     */
    @TableField(value = "doc_creator")
    private Long docCreator;

    /**
     * 回收截止时间
     */
    @TableField(value = "recycle_date")
    private Date recycleDate;

    /**
     * 回收状态 0:正常，1:已回收
     */
    @TableField(value = "recycle_status")
    private Integer recycleStatus;

    /**
     * 文件夹id
     */
    @TableField(value = "folder_id")
    private Long folderId;

    /**
     * 上传时间
     */
    @TableField(value = "upload_time")
    private Date uploadTime;

    /**
     * 下架时间
     */
    @TableField(value = "lower_time")
    private Date lowerTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1)
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;
}