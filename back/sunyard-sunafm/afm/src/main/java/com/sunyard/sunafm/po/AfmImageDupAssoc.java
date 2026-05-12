package com.sunyard.sunafm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 图像查重关联表;
 * </p>
 *
 * @author pjw
 * @since 2024-04-07
 */
@Getter
@Setter
@TableName("afm_image_dup_assoc")
public class AfmImageDupAssoc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 查重记录id
     */
    private Long dupNoteId;

    /**
     * 相似文件id
     */
    private Long assocExifId;

    /**
     * 相似度
     */
    private Double similarity;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
