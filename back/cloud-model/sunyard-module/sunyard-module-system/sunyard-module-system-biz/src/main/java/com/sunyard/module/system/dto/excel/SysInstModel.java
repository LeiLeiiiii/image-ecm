package com.sunyard.module.system.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author wubingyang
 * @date 2022/4/6 14:42
 */
@Data
public class SysInstModel {

    @ExcelProperty("上级机构")
    private String parentName;

    @ExcelProperty("机构名称")
    private String name;

    @ExcelProperty("机构号")
    private String instNo;

    @ExcelProperty("全宗号")
    private String identifier;

    @ExcelProperty("全宗名称")
    private String identifierName;

    @ExcelProperty("备注")
    private String remarks;
}
