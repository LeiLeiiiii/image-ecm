package com.sunyard.sunafm.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 检测记录-发票检测列表返回参
 *
 * @author P-JWei
 * @date 2024/3/11 16:08:39
 * @title
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class AfmDetNoteInvoiceListExcelDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 来源系统
     */
    @ExcelProperty(value = "来源系统", index = 0)
    @ColumnWidth(15)
    private String sourceSys;

    /**
     * 业务类型
     */
    @ExcelProperty(value = "业务类型", index = 1)
    @ColumnWidth(18)
    private String businessType;

    /**
     * 主索引
     */
    @ExcelProperty(value = "主索引", index = 2)
    @ColumnWidth(18)
    private String businessIndex;

    /**
     * 资料类型
     */
    @ExcelProperty(value = "资料类型", index = 3)
    @ColumnWidth(18)
    private String materialType;

    /**
     * 文件名称
     */
    @ExcelProperty(value = "文件名称", index = 4)
    @ColumnWidth(32)
    private String fileName;

    /**
     * 上传人
     */
    @ExcelProperty(value = "上传人", index = 5)
    @ColumnWidth(12)
    private String uploadUser;

    /**
     * 检测时间
     */
    @ExcelProperty(value = "检测时间", index = 6)
    @ColumnWidth(26)
    private Date invoiceDetTime;

    /**
     * 发票验真检测结果
     */
    @ExcelProperty(value = "发票验真检测结果", index = 7)
    @ColumnWidth(26)
    private String invoiceVerifyResultStr;

    /**
     * 发票查重检测结果
     */
    @ExcelProperty(value = "发票查重检测结果", index = 8)
    @ColumnWidth(26)
    private String invoiceDupResultStr;

    /**
     * 发票连续检测结果
     */
    @ExcelProperty(value = "发票连续检测结果", index = 9)
    @ColumnWidth(26)
    private String invoiceLinkResultStr;
}
