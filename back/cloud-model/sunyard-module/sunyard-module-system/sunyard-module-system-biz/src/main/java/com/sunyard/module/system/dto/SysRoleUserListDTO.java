package com.sunyard.module.system.dto;
/*
 * Project: sunyard
 *
 * File Created at 2025/9/16
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import lombok.Data;

/**
 * @author wml
 * @Desc
 * @date 2025/9/16 16:14
 */
@Data
public class SysRoleUserListDTO {

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 角色name
     */
    private String roleName;
}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2025/9/16 wml creat
 */