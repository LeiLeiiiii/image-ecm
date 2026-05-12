package com.sunyard.framework.onlyoffice.enums;

/**
 * 转换器错误代码
 * @author PJW
 */
public enum ConvertErrorCode {
    /**
     * 令牌无效
     */
    INVALID_TOKEN(-8, "令牌无效"),
    /**
     *输入错误
     */
    INPUT_ERROR(-7, "输入错误"),
    /**
     *访问转换结果数据库时出错
     */
    DATABASE_ERROR(-6, "访问转换结果数据库时出错"),
    /**
     *密码不正确
     */
    INCORRECT_PASSWORD(-5, "密码不正确"),
    /**
     *下载要转换的文档文件时出错
     */
    FILE_DOWNLOAD_ERROR(-4, "下载要转换的文档文件时出错"),
    /**
     *转换错误
     */
    CONVERSION_ERROR(-3, "转换错误"),
    /**
     *转换超时错误
     */
    TIMEOUT_ERROR(-2, "转换超时错误"),
    /**
     *未知错误
     */
    UNKNOWN_ERROR(-1, "未知错误");
    /**
     * 错误代码
     */
    private final int code;
    /**
     * 报错信息
     */
    private final String message;

    ConvertErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ConvertErrorCode getByCode(int code) {
        for (ConvertErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
