package com.sunyard.module.system.api.dto;
/*
 * Project: sunyard
 *
 * File Created at 2025/9/9
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

import java.io.Serializable;

/**
 * @author wml
 * @Desc
 * @date 2025/9/9 11:37
 */
@Data
public class SysPostDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long postId;

    private Long instId;

    private Long deptId;

    private Long userId;

    private String name;

    private String remarks;

    /**
     * 岗位代码
     */
    private String postCode;

}
/**
 * Revision history ------------------------------------------------------------------------- Date Author Note
 * ------------------------------------------------------------------------- 2025/9/9 wml creat
 */