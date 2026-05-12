package com.sunyard.sunafm.constant;

/**
 * @author scm
 * @since 2023/7/31 10:19
 * 策略管理枚举
 */
public enum DetNoteConstants {
    /**
     * OCR配置
     */
    REPEAT("疑似重复",0),

    /**
     * 提交时提醒
     */
    NORMAL("正常",1);
    private final Integer value;

    private final String description;

    DetNoteConstants(String description,Integer value) {
        this.description = description;
        this.value = value;
    }

    /**
     *
     * @return
     */
    public String description() {
        return description;
    }

    /**
     *
     * @return
     */
    public Integer value() {
        return value;
    }
}
