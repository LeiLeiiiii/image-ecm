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
public enum UserStateEnum {
    /**
     * 未启用
     */
    DISABLED(0, "未启用"),

    /**
     * 启用
     */
    ENABLED(1, "启用"),

    /**
     * 注销
     */
    CANCELLED(2, "注销"),

    /**
     * 锁定
     */
    LOCKED(3, "锁定");

    private Integer code;

    private String desc;

}
