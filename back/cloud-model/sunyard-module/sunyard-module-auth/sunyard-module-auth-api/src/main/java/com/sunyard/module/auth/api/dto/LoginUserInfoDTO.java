package com.sunyard.module.auth.api.dto;
/*
 * Project: com.sunyard.am.shiro.token
 *
 * File Created at 2021/7/2
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.shiro.token
 * @Desc
 * @date 2021/7/2 12:48
 */
@Data
public class LoginUserInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 用户登录部分 */
    /**
     * 用户id 加的依赖包序列化必须有id
     */
    private Long id;

    /**
     * 登录名
     */
    private String username;
    /**
     * 用户name
     */
    private String name;
    /**
     * 部门编号
     */
    private Long deptId;
    /**
     * 组织机构号
     */
    private Long instId;
    /**
     * 对外appid
     */
    private String appId;

    /**
     * 角色code
     */
    private String roleCode;

    /**
     * 机构code
     */
    private String orgCode;

    private List<Long> roleIdList;
    private List<String> roleCodeList;

    /**
     * 机构名称
     */
    private String orgName;
    /**
     * 账号类型 0普通,1系统管理员
     */
    private Integer loginType;

    /**
     * realm类型
     */
    private String realmType;

    /**
     * other
     */
    private String mobile;

    public Long getDeptId() {
        if (null == this.getLoginType() || this.getLoginType() != 0) {
            return null;
        }
        return deptId;
    }

    public Long getInstId() {
        if (null == this.getLoginType() || this.getLoginType() != 0) {
            return null;
        }
        return instId;
    }

}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/2 zhouleibin creat
 */
