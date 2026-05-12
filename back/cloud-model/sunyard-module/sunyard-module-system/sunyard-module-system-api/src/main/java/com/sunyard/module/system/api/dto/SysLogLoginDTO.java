package com.sunyard.module.system.api.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 操作日志表
 * </p>
 *
 * @author liugang
 * @since 2021-12-02
 */
@Data
public class SysLogLoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 主键
     */
    private Long id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 登录ip
     */
    private String loginIp;

    /**
     * 登录浏览器
     */
    private String loginBrowser;

    /**
     * 登录系统
     */
    private String loginSystem;

    /**
     * 登录状态（0成功、1失败）
     */
    private Integer loginStatus;
    private String loginStatusStr;

    /**
     * 登录说明信息
     */
    private String loginMsg;

    /**
     * 登录日期
     */
    private Date loginTime;
    /**
     * 分页计算字段
     */
    private Integer size;
    /**
     * 页数
     */
    private Integer pageSize;

    /**
     * 日志开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginDateStart;

    /**
     * 日志结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginDateEnd;
}
