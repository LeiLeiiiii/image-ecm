package com.sunyard.module.system.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author liugang
 * @since 2022-02-09
 */
@Data
public class SysConfigLoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 数据源描述
     */
    private String sourceDescribe;

    /**
     * ip地址
     */
    private String serverUrl;

    /**
     * 服务器Dn
     */
    private String serverDn;

    /**
     * 组织架构
     */
    private String base;

    /**
     * 用户
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 登录方式 0：本地 1：ldap
     */
    private Long loginType;

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
     * 是否激活(否:0,是:1)
     */
    private Integer isActivate;

}
