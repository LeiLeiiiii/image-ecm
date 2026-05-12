package com.sunyard.module.system.dto.excel;

import java.io.Serializable;
import java.util.Date;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;

import lombok.Data;

/**
 * <p>
 * 登录日志表
 * </p>
 *
 * @author liugang
 * @since 2021-12-02
 */
@Data
public class LoginLogModel implements Serializable {

    private static final long serialVersionUID = 1L;



    /**
     * 用户名称
     */
    @ExcelProperty("用户名称")
    private String userName;

    /**
     * 登录ip
     */
    @ExcelProperty("登录IP")
    private String loginIp;

    /**
     * 登录浏览器
     */
    @ExcelProperty("登录浏览器")
    private String loginBrowser;

    /**
     * 登录系统
     */
    @ExcelProperty("登录系统")
    private String loginSystem;

    /**
     * 登录状态
     */
    @ExcelProperty("登录状态")
    private String loginStatusStr;

    /**
     * 登录状态（0成功、1失败）
     */
    @ExcelIgnore
    private Integer loginStatus;

    /**
     * 登录说明信息
     */
    @ExcelProperty("登录说明信息")
    private String loginMsg;

    /**
     * 登录日期
     */
    @ExcelProperty("登录日期")
    private Date loginTime;

    /**
     * 创建时间
     */
    @ExcelProperty("登录时间")
    private Date createTime;

}
