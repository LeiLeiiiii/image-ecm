package com.sunyard.framework.common.exception;

/**
 * @author liugang
 * @date 9:50 2021/11/4
 */
public class ApiException extends RuntimeException {

    private Integer code;

    private String msg;

    public ApiException() {
        super();
    }

    public ApiException(Integer code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }

    public ApiException(String msg, Integer code) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
