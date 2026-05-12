package com.sunyard.module.system.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author wubingyang
 * @date 2022/4/2 9:39
 */
@Data
public class SysUserModel {

    @ExcelProperty("登录用户名")
    private String loginName;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("所属部门")
    private String deptName;

    @ExcelProperty("所属机构")
    private String instName;

    @ExcelProperty("员工编号")
    private String code;

    @ExcelProperty("用户性别")
    private String sex;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

}
