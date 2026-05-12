package com.sunyard.framework.onlyoffice.enums;

/**
 * 错误代码
 * @author PJW
 */
public enum ErrorCode {
    /**
     * 缺少文档密钥或找不到具有此类密钥的文档
     */
    MISSING_DOCUMENT_KEY(1, "缺少文档密钥或找不到具有此类密钥的文档"),
    /**
     * 回调网址不正确
     */
    INCORRECT_CALLBACK_URL(2, "回调网址不正确"),
    /**
     * 内部服务器错误
     */
    INTERNAL_SERVER_ERROR(3, "内部服务器错误"),
    /**
     * 在收到强制保存命令之前，未对文档应用任何更改
     */
    NO_CHANGES_BEFORE_FORCE_SAVE(4, "在收到强制保存命令之前，未对文档应用任何更改"),
    /**
     * 命令不正确
     */
    INCORRECT_COMMAND(5, "命令不正确"),
    /**
     * 令牌无效
     */
    INVALID_TOKEN(6, "令牌无效");
    /**
     * 错误代码
     */
    private final int code;
    /**
     * 错误信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }
}
