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
public enum LoginLocalEnum {
    // 本地联调模式
    LOCAL("local"),
    // 本地用户模式
    LOCAL_USER("localUser"),
    // 远程注册模式
    REMOTE("remote");

    private String value;

    LoginLocalEnum(String value) {
        this.value = value;
    }

    public static LoginLocalEnum getType(String code) {
        for (LoginLocalEnum enums : LoginLocalEnum.values()) {
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
