package com.sunyard.framework.common.result;
/*
 * Project: com.sunyard.am.result
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

/**
 * @author zhouleibin
 * @date 2021/6/30 8:19
 */
public enum ResultCode {
    // 2xx-成功
    SUCCESS(200, "操作成功"),

    // 4xx-客户端请求错误
    PARAM_ERROR(400, "输入参数不合法"),
    NO_LOGIN_AUTH(401, "请重新登录"),
    NO_OPERATION_AUTH(402, "缺少操作权限"),
    NO_DATA_AUTH(403, "缺少数据权限"),
    NO_READ_AUTH(4033, "暂无查看权限"),
    // 5xx-服务器错误
    SYSTEM_ERROR(500, "系统异常"),
    SYSTEM_BUSY_ERROR(501, "系统繁忙"),
    // 6xx-第三方系统连接错误
    SYSTEM_EM_ERROR(601, "连接影像系统异常"),
    //
    OSS_FAIL_SIZE(600001, "分片数量不一致无法合并");

    private Integer code;
    private String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ResultCode getEnum(Integer code) {
        for (ResultCode retEnum : ResultCode.values()) {
            if (retEnum.getCode().equals(code)) {
                return retEnum;
            }
        }
        return null;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
