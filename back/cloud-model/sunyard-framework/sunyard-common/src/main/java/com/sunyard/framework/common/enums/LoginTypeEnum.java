package com.sunyard.framework.common.enums;
/*
 * Project: SunAM
 *
 * Copyright 2016 Corporation Limited. All rights reserved.
 *
 * This software is the confidential and proprietary information of Company. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of the license.
 */

/**
 * @author zhouleibin
 * @date 2022/2/7 15:50
 * @Desc
 */
public enum LoginTypeEnum {
    // 本地
    LOCAL("local"),
    // ldap协议方式
    LDAP("ldap"),
    // 超管
    SUPER("super");

    private String value;

    LoginTypeEnum(String value) {
        this.value = value;
    }

    public static LoginTypeEnum getType(String code) {
        for (LoginTypeEnum enums : LoginTypeEnum.values()) {
            if (enums.getValue().equals(code)) {
                return enums;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2022/2/7 zhouleibin creat
 */
