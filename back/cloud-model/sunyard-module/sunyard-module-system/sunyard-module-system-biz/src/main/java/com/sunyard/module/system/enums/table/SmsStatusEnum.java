package com.sunyard.module.system.enums.table;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * sys_msg
 * 已读状态 *
 * @author leo
 * @date 2026/01/01 16:50
 */
@Getter
@AllArgsConstructor
public enum SmsStatusEnum {

    /**
     * 未读
     */
    UNREAD(0, "未读"),

    /**
     * 已读
     */
    READ(1, "已读");

    private Integer code;

    private String desc;

}
