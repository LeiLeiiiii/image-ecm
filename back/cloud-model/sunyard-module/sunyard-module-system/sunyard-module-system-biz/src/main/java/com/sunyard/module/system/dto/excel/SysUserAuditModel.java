package com.sunyard.module.system.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 用户审计导出
 * </p>
 * @author PJW
 */
@Data
public class SysUserAuditModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty("用户名称")
    private String userName;

    @ExcelProperty("登录成功次数")
    private Integer loginNum;

    @ExcelProperty("登录失败次数")
    private Integer loginFalseNum;

    @ExcelProperty("接口访问次数")
    private Integer apiNum;

    @ExcelProperty("后台访问次数")
    private Integer sysLogNum;

    @ExcelProperty("审计开始日期")
    private Date auditStartTime;

    @ExcelProperty("审计结束日期")
    private Date auditEndTime;

    @ExcelProperty("创建时间")
    private Date createTime;

}
