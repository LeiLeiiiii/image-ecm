package com.sunyard.mytool.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文档标签关联表实体类
 */
@TableName("doc_bs_tag_document")
@Data
public class DocBsTagDocument {

    /**
     * 主键id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 文档id
     */
    @TableField(value = "doc_id")
    private Long docId;

    /**
     * 标签id
     */
    @TableField(value = "tag_id")
    private Long tagId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 删除状态(否:0,是:1)
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;
}
