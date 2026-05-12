package com.sunyard.module.system.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * <p>
 * 用户审计记录表
 * </p>
 * @author PJW
 */
@Data
public class SysUserAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;


    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 登录次数
     */
    private Integer loginNum;

    /**
     * 接口次数
     */
    private Integer apiNum;

    /**
     * 访问次数最多的接口地址
     */
    private String firstApiUrl;

    /**
     * 审计范围开始时间
     */
    private Date auditStartTime;

    /**
     * 审计范围结束时间
     */
    private Date auditEndTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除状态(否:0,是:1)
     */
    private Integer isDeleted;

    /**
     * 登录失败次数
     */
    private Integer loginFalseNum;

    /**
     * 后台访问次数
     */
    private Integer sysLogNum;
}
