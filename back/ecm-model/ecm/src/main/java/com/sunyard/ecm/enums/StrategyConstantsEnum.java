package com.sunyard.ecm.enums;

/**
 * @author scm
 * @since 2023/7/31 10:19
 * @desc 策略管理枚举
 */
public enum StrategyConstantsEnum {
    /**
     * OCR配置
     */
    OCR_STRATEGY("OCR识别"),

    /**
     * 提交时提醒
     */
    REMIND("提交时提醒"),

    /**
     * 提交时阻断
     */
    BLOCK("提交时阻断"),

    /**
     * 允许提交
     */
    ALLOW("允许提交"),
    ;

    private final String description;

    StrategyConstantsEnum(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     */
    public String description() {
        return description;
    }
}
