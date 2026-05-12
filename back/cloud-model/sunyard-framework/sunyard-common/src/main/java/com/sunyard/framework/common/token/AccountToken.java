package com.sunyard.framework.common.token;
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

import java.util.Date;
import java.util.List;

import org.apache.shiro.authc.UsernamePasswordToken;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhouleibin
 * @date 2021/7/2 12:48
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AccountToken extends UsernamePasswordToken {
    /** 用户登录部分 */
    /**
     * 用户id 加的依赖包序列化必须有id
     */
    private Long id;
    /** 用户name */
    private String name;
    /** 部门编号 */
    private Long deptId;
    /** 部门编号 */
    private String deptCode;
    /** 组织机构号 */
    private Long instId;
    /** 账号类型 0普通,1系统管理员 */
    private Integer loginType;
    /** realm类型 */
    private String realmType;

    private List<Long> roleIdList;
    private List<String> roleCodeList;
    /**
     * 最后修改时间
     */
    private Date updateTime;

    /** other */
    /** 用户账号 */
    private String mobile;


    /**
     * 是否是第三方跳转过来的,默认否
     */
    private boolean isOut = false;
    /**
     * 用户会话唯一标示
     */
    private String flagId;

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

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 密码更新时间
     */
    private Date pwdUpdateTime;

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
