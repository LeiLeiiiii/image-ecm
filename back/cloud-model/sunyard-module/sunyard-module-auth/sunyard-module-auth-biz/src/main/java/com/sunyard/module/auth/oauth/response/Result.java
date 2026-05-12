package com.sunyard.module.auth.oauth.response;
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

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.result
 * @Desc
 * @date 2021/6/30 8:17
 */
@Data
@ToString
public class Result<T> implements Serializable {

    private String code;
    private String msg;
    private T data;

    /**
     * 成功返参
     * @param value 数据
     * @return result
     * @param <T> 参数类型
     */
    public static <T> Result<T> success(T value) {
        Result<T> result = new Result<>();
        result.setData(value);
        result.setMsg(ResponseCodeEnum.SUCCESS.getMsg());
        result.setCode(ResponseCodeEnum.SUCCESS.getCode());
        return result;
    }

    /**
     * 错误返参
     * @param responseCodeEnum 响应头code类型
     * @return result
     * @param <T> 入参类型
     */
    public static <T> Result<T> error(ResponseCodeEnum responseCodeEnum) {
        Result<T> result = new Result<>();
        result.setMsg(responseCodeEnum.getMsg());
        result.setCode(responseCodeEnum.getCode());
        return result;
    }

    /**
     *  错误返参
     * @param message 消息
     * @param responseCodeEnum 响应头code类型
     * @return result
     * @param <T> 入参类型
     */
    public static <T> Result<T> error(String message, ResponseCodeEnum responseCodeEnum) {
        Result<T> result = new Result<>();
        result.setData(null);
        if (null == message || 0 == message.length()) {
            result.setMsg(responseCodeEnum.getMsg());
        } else {
            result.setMsg(message);
        }
        result.setCode(responseCodeEnum.getCode());
        return result;
    }

    /**
     * 创建自定义的回传信息
     *
     * @param code 回执代号
     * @param msg 回执信息
     * @param <T> 数据类型
     * @return Result 实例
     */
    public static <T> Result<T> error(String msg, String code) {
        Result<T> result = new Result<>();
        result.setData(null);
        result.setMsg(msg);
        result.setCode(code);
        return result;
    }

    /**
     * 判断是否成功
     * @return boolean
     */
    public boolean isSucc() {
        return ResponseCodeEnum.SUCCESS.getCode().equals(this.code);
    }


}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
