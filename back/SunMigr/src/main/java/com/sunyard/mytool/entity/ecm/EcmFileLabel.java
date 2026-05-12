package com.sunyard.mytool.entity.ecm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * ecm_file_label - 文件标签信息实体类
 */
@Data
@TableName("ecm_file_label")
public class EcmFileLabel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.NONE)
    private Long id;

    /**
     * 业务id
     */
    @TableField(value = "busi_id")
    private Long busiId;

    /**
     * 文件id
     */
    @TableField(value = "file_id")
    private Long fileId;

    /**
     * 标签名称
     */
    @TableField(value = "label_name")
    private String labelName;

    /**
     * 关联的标签id
     */
    @TableField(value = "label_id")
    private Long labelId;
}
