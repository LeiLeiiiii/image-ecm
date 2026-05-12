package com.sunyard.module.system.vo;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.io.Serializable;

import lombok.Data;

/**
 * @author zhouleibin
 * @date 2021/8/20 22:32
 * @Desc
 */
@Data
public class SysRoleVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long roleId;
    private Long instId;
    private String name;
    private String remarks;
    private Integer status;
    private Long[] menuIds;
    /**
     * 角色代码
     */
    private String roleCode;

    /**
     * 系统区分：0档案，1影像
     */
    private String systemCode;
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/8/20 zhouleibin creat
 */
