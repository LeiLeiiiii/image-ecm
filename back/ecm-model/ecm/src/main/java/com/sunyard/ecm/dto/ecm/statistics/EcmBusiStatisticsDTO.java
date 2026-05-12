package com.sunyard.ecm.dto.ecm.statistics;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 @Author 朱山成
 @time 2024/6/13 10:20
 **/
@Data
@Accessors(chain = true)
public class EcmBusiStatisticsDTO implements Serializable {
    /**
     * 业务总量
     */
    @ExcelIgnore
    private Long busiTotal;

    /**
     * 文件总量
     */
    @ExcelIgnore
    private Long fileTotal;
    /**
     * 存储总量
     */
    @ExcelIgnore
    private Long storageTotal;
    /**
     * 存储总量
     */
    @ExcelIgnore
    private Double storageTotalGb;

    /**
     * 业务号
     */
    @ApiModelProperty(value = "业务号")
    @ExcelIgnore
    private String appCode;

    @ApiModelProperty(value = "业务名称")
    @ExcelProperty(value = "业务类型",index = 1)
    private String appName;
    /**
     * 值  业务量
     */
    @ExcelIgnore
    private Long count;

    /**
     * 日期  日 MM-dd 月M  年yyyy
     */
    @ApiModelProperty(value = "创建时间")
    @ExcelProperty(value = "日期",index = 2)
    private String createDate;

    @ApiModelProperty(value = "主键")
    @ExcelIgnore
    private Long id;

    @ApiModelProperty(value = "机构号")
    @ExcelProperty(value = "机构号",index = 0)
    private String orgCode;

    @ApiModelProperty(value = "当天新增业务数量")
    @ExcelProperty(value = "业务量（笔)",index = 3)
    private Long daySize;

    @ApiModelProperty(value = "当天新增文件数量")
    @ExcelProperty(value = "文件数量（个)",index = 4)
    private Long fileNumber;

    @ApiModelProperty(value = "文件总大小(GB)")
    @ExcelProperty(value = "存储量（GB)",index = 5)
    private Double fileSize;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat("yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ExcelIgnore
    private Date createTime;

    @ApiModelProperty(value = "时间字符串形式(yyyyMMdd)")
    @ExcelIgnore
    private String insertTime;

    @ApiModelProperty(value = "更新时间")
    @ExcelIgnore
    private Date updateTime;
}
