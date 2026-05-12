package com.sunyard.framework.common.result;

public enum SsoResultCode {

    SUCCESS(0, "操作成功"),
    PARAM_MISSING(101, "参数缺失错误，appId、appSecret、userId、password都为必填"),
    AUTH_FAILED(102, "接口认证错误，非ChinaLink平台颁发的appId、appSecret"),
    USER_NOT_EXISTS(103, "账号信息异常；接收到的用户账号不存在"),
    PASSWORD_POLICY_VIOLATION(104, "密码修改异常；接收到的密码不符合系统密码策略"),
    SYSTEM_ERROR(999, "其他异常");

    private final Integer code;
    private final String message;

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    SsoResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
