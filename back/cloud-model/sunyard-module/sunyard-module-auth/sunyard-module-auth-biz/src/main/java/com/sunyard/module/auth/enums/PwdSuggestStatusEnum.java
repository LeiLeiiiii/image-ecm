package com.sunyard.module.auth.enums;
/*
 *
 * File Created at 2026/1/13
 *
 * Copyright 2016 Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Desc 密码修改提示状态枚举
 * @author Leo
 * @date 2026/1/13 15:50
 * @version
 */
@Getter
@AllArgsConstructor
public enum PwdSuggestStatusEnum {
    NORMAL(0, "正常"),
    EXPIRE_WARNING(1, "您的密码已超过xx天未修"),
    CHANGE_INIT_PASSWORD(2, "请修改初始密码");

    private Integer code;
    private String desc;

    public static String getStatusDesc(Integer status) {
        for (PwdSuggestStatusEnum value : PwdSuggestStatusEnum.values()) {
            if (value.getCode().equals(status)) {
                return value.getDesc();
            }
        }
        return null;
    }

    public static PwdSuggestStatusEnum getEnum(Integer status) {
        for (PwdSuggestStatusEnum value : PwdSuggestStatusEnum.values()) {
            if (value.getCode().equals(status)) {
                return value;
            }
        }
        return null;
    }
}
/**
 * Revision history
 * -------------------------------------------------------------------------
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2026/1/13 Leo creat
 */
