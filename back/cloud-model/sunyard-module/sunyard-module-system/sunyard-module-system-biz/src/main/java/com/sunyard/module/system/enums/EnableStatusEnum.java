package com.sunyard.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 启用状态枚举
 *
 * @author leo
 * @date 2025/01/01 16:50
 */
@Getter
@AllArgsConstructor
public enum EnableStatusEnum {

    /**
     * 未启用
     */
    DISABLED(0, "未启用"),

    /**
     * 已启用
     */
    ENABLED(1, "已启用");

    private Integer code;

    private String desc;

    public static EnableStatusEnum getEnum(Integer code) {
        for (EnableStatusEnum checkTypeEnum : EnableStatusEnum.values()) {
            if (checkTypeEnum.getCode().equals(code)) {
                return checkTypeEnum;
            }
        }
        return null;
    }

}
