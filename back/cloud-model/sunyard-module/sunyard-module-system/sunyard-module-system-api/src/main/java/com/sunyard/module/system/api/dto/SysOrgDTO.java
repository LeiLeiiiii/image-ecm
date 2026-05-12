package com.sunyard.module.system.api.dto;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouleibin
 * @date 2021/8/25 17:15
 * @Desc
 */
@Data
public class SysOrgDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父id
     */
    private Long parentId;

    /**
     * 1机构-2部门
     */
    private Integer type;

    /**
     * 部门
     */
    private Long deptId;

    /**
     * 机构
     */
    private Long instId;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 机构或部门 文档用
     */
    private Long relId;

    /**
     * 类型 文档用
     */
    private Integer relType;

    /**
     * 机构号
     */
    private String instNo;
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/8/25 zhouleibin creat
 */
