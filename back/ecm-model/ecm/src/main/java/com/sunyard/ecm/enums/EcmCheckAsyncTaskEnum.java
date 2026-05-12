package com.sunyard.ecm.enums;

/**
 * @author yzy
 * @desc
 * @since 2025/3/10
 */
public enum EcmCheckAsyncTaskEnum {

    /**
     * 初始状态
     */
    INITIAL_STATE("0"),

    /**
     * 处理中
     */
    PROCESSING("1"),

    /**
     * 失败
     */
    FAILED("2"),

    /**
     * 检测通过
     */
    SUCCESS("3"),

    /**
     * 排除异常
     */
    EXCLUDE_ANOMALY("4"),

    /**
     * 确认异常
     */
    CONFIRM_ANOMALY("5"),

    /**
     * 检测异常，待审核
     */
    CHECK_FAILED("6"),

    /**
     * 拆分成功
     */
    SPLIT_SUCCESS("7"),


    /**
     * 合并成功
     */
    MERGE_SUCCESS("8"),

    /**
     * 转正识别不需要处理
     */
    NOT_DEAL("9"),


    /**
     * 已存入队列
     */
    IN_MQ("I"),
    ;




    private final String description;

    EcmCheckAsyncTaskEnum(String description) {
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
