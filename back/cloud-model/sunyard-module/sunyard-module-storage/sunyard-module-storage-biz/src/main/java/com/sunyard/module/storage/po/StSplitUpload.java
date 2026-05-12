package com.sunyard.module.storage.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 分片上传临时表
 * </p>
 *
 * @author yzy
 * @since 2024-10-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StSplitUpload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 上传ID
     */
    private String uploadId;

    /**
     * 分片大小
     */
    private Long chunkSize;

    /**
     * 分片数量
     */
    private Integer chunkNum;

    /**
     * 是否上传成功
     */
    private Integer isUploadOk;


    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
