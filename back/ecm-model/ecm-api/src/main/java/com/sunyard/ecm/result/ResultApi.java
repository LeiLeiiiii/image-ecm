package com.sunyard.ecm.result;
/*
 * Project: com.sunyard.am.ResultApi
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.sunyard.ecm.enums.ResultCodeApiEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.ResultApi
 * @Desc
 * @date 2021/6/30 8:17
 */
@Data
public class ResultApi<T> implements Serializable {

    private String code;
    private String msg;
    private T data;

    /***/
    public static <T> ResultApi<T> success(T value) {
        ResultApi<T> resultApi = new ResultApi<>();
        resultApi.setData(value);
        resultApi.setMsg(ResultCodeApiEnum.SUCCESS.getMsg());
        resultApi.setCode(ResultCodeApiEnum.SUCCESS.getCode());
        return resultApi;
    }


    /***/
    public static <T> ResultApi<T> success(String message, ResultCodeApiEnum resultCodeApiEnum) {
        ResultApi<T> resultApi = new ResultApi<>();
        resultApi.setData(null);
        if (null == message || 0 == message.length()) {
            resultApi.setMsg(resultCodeApiEnum.getMsg());
        } else {
            resultApi.setMsg(message);
        }
        resultApi.setCode(resultCodeApiEnum.getCode());
        return resultApi;
    }

    /***/
    public static <T> ResultApi<T> error(ResultCodeApiEnum resultCodeApiEnum) {
        ResultApi<T> resultApi = new ResultApi<>();
        resultApi.setData(null);
        resultApi.setMsg(resultCodeApiEnum.getMsg());
        resultApi.setCode(resultCodeApiEnum.getCode());
        return resultApi;
    }

    /***/
    public static <T> ResultApi<T> error(String message, ResultCodeApiEnum resultCodeApiEnum) {
        ResultApi<T> resultApi = new ResultApi<>();
        resultApi.setData(null);
        if (null == message || 0 == message.length()) {
            resultApi.setMsg(resultCodeApiEnum.getMsg());
        } else {
            resultApi.setMsg(message);
        }
        resultApi.setCode(resultCodeApiEnum.getCode());
        return resultApi;
    }

    /**
     * 创建自定义的回传信息
     *
     * @param code 回执代号
     * @param msg  回执信息
     * @param <T>  数据类型
     * @return 实例
     */
    public static <T> ResultApi<T> error(String msg, String code) {
        ResultApi<T> resultApi = new ResultApi<>();
        resultApi.setData(null);
        resultApi.setMsg(msg);
        resultApi.setCode(code);
        return resultApi;
    }

    /***/
    public boolean isSucc() {
        return ResultCodeApiEnum.SUCCESS.getCode().equals(this.code);
    }

}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
