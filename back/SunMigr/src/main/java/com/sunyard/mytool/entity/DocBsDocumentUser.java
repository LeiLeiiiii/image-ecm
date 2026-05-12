package com.sunyard.mytool.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 文件、文档和用户关联表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("doc_bs_document_user")
public class DocBsDocumentUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件夹id
     */
    @TableField("doc_id")
    private Long docId;

    /**
     * 用户id/部门id/机构id/团队id
     */
    @TableField("rel_id")
    private Long relId;

    /**
     * 关联的类型，0:用户、1:机构、2:部门、3:团队
     */
    @TableField("type")
    private Integer type;

    /**
     * 权限，0:可查看，1:可编辑，2：可管理
     */
    @TableField("permiss_type")
    private Integer permissType;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 删除状态(否:0,是:1)
     */
    @TableLogic
    private Integer isDeleted;

}
