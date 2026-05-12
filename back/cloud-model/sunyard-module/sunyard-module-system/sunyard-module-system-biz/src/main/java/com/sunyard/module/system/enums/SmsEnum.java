package com.sunyard.module.system.enums;
/*
 * Project: sunyard-cloud
 *
 * File Created at 2025/8/25
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
 * @author zjm
 * @Desc
 * @date 2025/8/25 17:56
 */
@Getter
@AllArgsConstructor
public enum SmsEnum {
    /** 档案到保管期限后发送 */
    SMS_EXPIRE("CDB-SMS-CHANNEL-20250821000"),
    /** 收集档案未及时归档后发送 */
    SMS_UNFILED("CDB-SMS-CHANNEL-20250821001"),
    /** 移交清单被驳回后发送 */
    SMS_REJECT("CDB-SMS-CHANNEL-20250821002"),
    /** 文件未及时上传发送 */
    SMS_NotUPLOADED("CDB-SMS-CHANNEL-20250821003");

    private String value;

    public static SmsEnum getType(String code) {
        for (SmsEnum enums : SmsEnum.values()) {
            if (enums.getValue().equals(code)) {
                return enums;
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
 * 2025/8/25 mm creat
 */
