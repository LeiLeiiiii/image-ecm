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

import java.io.Serializable;

import lombok.Data;

/**
 * @author zhouleibin
 * @date 2021/6/30 8:17
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    /**
     * 成功-无data
     *
     * @param <T> 返回内容对象
     * @return Result
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        return result;
    }

    /**
     *  成功-有data
     * @param value 返回内容
     * @return Result
     * @param <T> 返回内容对象
     */
    public static <T> Result<T> success(T value) {
        Result<T> result = new Result<>();
        result.setData(value);
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setCode(ResultCode.SUCCESS.getCode());
        return result;
    }

    /**
     * 成功-有data并指定code、msg
     * @param value 内容
     * @param code code
     * @param msg 描述
     * @return result
     * @param <T> 返回内容对象
     */
    public static <T> Result<T> success(T value, Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setData(value);
        result.setMsg(msg);
        result.setCode(code);
        return result;
    }


    /**
     * 失败-有data并指定code、msg
     * @param value 内容
     * @param code code
     * @param msg 描述
     * @return result
     * @param <T> 返回内容对象
     */
    public static <T> Result<T> error(T value, String msg, Integer code) {
        Result<T> result = new Result<>();
        result.setData(value);
        result.setMsg(msg);
        result.setCode(code);
        return result;
    }


    /**
     * 失败-指定code
     * @param resultCode code
     * @return result
     * @param <T> 返回内容对象
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setData(null);
        result.setMsg(resultCode.getMsg());
        result.setCode(resultCode.getCode());
        return result;
    }

    /**
     * 失败-指定code、msg
     * @param message 描述
     * @param resultCode code
     * @return result
     * @param <T> 返回内容对象
     */
    public static <T> Result<T> error(String message, ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setData(null);
        if (null == message || 0 == message.length()) {
            result.setMsg(resultCode.getMsg());
        } else {
            result.setMsg(message);
        }
        result.setCode(resultCode.getCode());
        return result;
    }

    /**
     * 创建自定义的回传信息
     *
     * @param code 回执代号
     * @param msg  回执信息
     * @param <T>  数据类型
     * @return Result 实例
     */
    public static <T> Result<T> error(String msg, Integer code) {
        Result<T> result = new Result<>();
        result.setData(null);
        result.setMsg(msg);
        result.setCode(code);
        return result;
    }

    /**
     * 判断是否成功
     * @return boo
     */
    public boolean isSucc() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }


    public static <T> Result<T> ssoResponse(SsoResultCode ssoResultCode) {
        Result<T> result = new Result<>();
        result.setData(null);
        result.setMsg(ssoResultCode.getMessage());
        result.setCode(ssoResultCode.getCode());
        return result;
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
