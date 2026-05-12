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
 * 检测记录-图像查重列表返回参
 * @author P-JWei
 * @date 2024/3/11 16:31:01
 * @title
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class AfmDetNoteImgListExelDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 来源系统
     */
    @ExcelProperty(value = "来源系统",index = 0)
    @ColumnWidth(15)
    private String sourceSys;

    /**
     * 业务类型
     */
    @ExcelProperty(value = "业务类型",index = 1)
    @ColumnWidth(18)
    private String businessType;

    /**
     * 主索引
     */
    @ExcelProperty(value = "主索引",index = 2)
    @ColumnWidth(18)
    private String businessIndex;

    /**
     * 资料类型
     */
    @ExcelProperty(value = "资料类型",index = 3)
    @ColumnWidth(18)
    private String materialType;

    /**
     * 文件id
     */
    @ExcelProperty(value = "文件id",index = 4)
    @ColumnWidth(26)
    private String exifIdOrMd5;

    /**
     * 文件名称
     */
    @ExcelProperty(value = "文件名称",index = 5)
    @ColumnWidth(32)
    private String fileName;

    /**
     * 上传人
     */
    @ExcelProperty(value = "上传人",index = 6)
    @ColumnWidth(12)
    private String uploadUserName;

    /**
     * 查重时间
     */
    @ExcelProperty(value = "查重时间",index = 7)
    @ColumnWidth(26)
    private Date imgDupTime;

    /**
     * 查重结果（字典：0、1）
     */
    @ExcelProperty(value = "最高相似度",index = 8)
    @ColumnWidth(18)
    private Double imgDupResult;

    /**
     * 查重结果（字典值：正常、疑似重复）
     */
    @ExcelProperty(value = "查重结果",index = 9)
    @ColumnWidth(12)
    private String imgDupResultStr;

}
