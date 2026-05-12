package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * ecm_busi_doc - 动态树资料信息实体类
 */
@Data
@TableName("ecm_busi_doc")
public class EcmBusiDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "doc_id", type = IdType.NONE)
    private Long docId;

    /**
     * 业务表主键
     */
    @TableField(value = "busi_id")
    private Long busiId;

    /**
     * 资料类型code
     */
    @TableField(value = "doc_code")
    private String docCode;

    /**
     * 资料名称
     */
    @TableField(value = "doc_name")
    private String docName;

    /**
     * 资料顺序（DOC_TYPE_ID下排序）
     */
    @TableField(value = "doc_sort")
    private Float docSort;

    /**
     * 标记标志（默认0，标记1）
     */
    @TableField(value = "doc_mark")
    private Integer docMark;

    /**
     * 资料父节点code
     */
    @TableField(value = "parent_id")
    private Long parentId;
}
