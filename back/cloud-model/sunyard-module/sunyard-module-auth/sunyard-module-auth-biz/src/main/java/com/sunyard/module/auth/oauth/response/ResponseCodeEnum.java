package com.sunyard.module.auth.oauth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author P-JWei
 * @date 2023/7/17 10:20
 * @title 响应码
 * @description
 */
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum {
    /**
     *成功
     */
    SUCCESS("00000","成功"),
    /**
     *APPID未授权
     */
    FAIL_UNAUTH("C0001","APPID未授权"),
    /**
     *APPID校验失败
     */
    FAIL_APPID("C0002","APPID校验失败"),
    /**
     *无接口权限
     */
    FAIL_NOAPI("C0003","无接口权限"),
    /**
     *验签失败
     */
    FAIL_SIGN("S0001","验签失败"),
    /**
     *数据解密失败
     */
    FAIL_DECRYPT("S0002","数据解密失败"),
    /**
     *时间戳校验失败
     */
    FAIL_TIMESTAMP("S0003","时间戳校验失败"),
    /**
     *REFERER校验失败
     */
    FAIL_REFERER("S0004","REFERER校验失败"),
    /**
     *请求速率达到阀值
     */
    FAIL_MAXNUM("S0005","请求速率达到阀值"),
    /**
     *TOKEN无效
     */
    FAIL_TOKEN("S0006","TOKEN无效"),
    /**
     *TOKEN无此接口权限
     */
    FAIL_TOKENNOAPI("S0007","TOKEN无此接口权限");

    /**
     * 错误代码
     */
    private String code;
    /**
     * 错误信息
     */
    private String msg;

}
