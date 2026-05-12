package com.sunyard.module.system.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author wubingyang
 * @date 2022/4/2 9:39
 */
@Data
public class SysDeptModel {

    @ExcelProperty("部门名称")
    private String name;

    @ExcelProperty("部门号")
    private String deptNo;

    @ExcelProperty("所属机构")
    private String instName;

    @ExcelProperty("上级部门")
    private String parentName;
}
