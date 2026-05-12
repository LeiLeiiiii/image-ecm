package com.sunyard.sunafm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 发票检测记录关联表;
 * </p>
 *
 * @author pjw
 * @since 2024-04-19
 */
@Getter
@Setter
@TableName("afm_invoice_det_note_assoc")
public class AfmInvoiceDetNoteAssoc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发票检测记录id
     */
    private Long invoiceNoteId;

    /**
     * 关联文件id
     */
    private Long assocExifId;

    /**
     * 关联文件类型（0重复 1连续）
     */
    private Integer assocType;

    /**
     * 创建时间
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
}
