package com.sunyard.sunafm.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 发票文件详情信息;
 * </p>
 *
 * @author pjw
 * @since 2024-04-07
 */
@Getter
@Setter
@TableName("afm_invoice_file_data")
public class AfmInvoiceFileData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件md5
     */
    private String fileMd5;

    /**
     * 发票代码
     */
    private String invoiceCode;

    /**
     * 发票号
     */
    private String invoiceNum;

    /**
     * 发票校验码
     */
    private String invoiceCheckCode;

    /**
     * 发票日期
     */
    private String invoiceDate;

    /**
     * 发票类型
     */
    private String invoiceType;

    /**
     * 发票金额
     */
    private String invoiceTotal;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 是否删除（0否 1是）
     */
    @TableLogic
    private Integer isDeleted;
}
