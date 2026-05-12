package com.sunyard.module.system.api.dto;
/*
 * Project: am
 *
 * File Created at 2021/7/15
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import lombok.Data;

import java.io.Serializable;

/**
 * @author wubingyang
 * @date 2022/4/2 9:39
 */
@Data
public class SysUserExportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 登录用户名
     */
    private String loginName;

    /**
     * 姓名
     */
    private String name;

    /**
     * 所属部门
     */
    private String deptName;

    /**
     * 所属机构
     */
    private String instName;

    /**
     * 员工编号
     */
    private String code;

    /**
     * 用户性别
     */
    private String sex;

    /**
     * 用户联系电话
     */
    private String phone;
    /**
     * 用户电子邮箱
     */
    private String email;

}
