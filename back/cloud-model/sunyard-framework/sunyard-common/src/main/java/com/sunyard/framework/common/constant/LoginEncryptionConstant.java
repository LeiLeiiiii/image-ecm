package com.sunyard.framework.common.constant;

public class LoginEncryptionConstant {

    /**
     * rsa登录加密解密方式
     */
    public static final int RSA_LOGIN_TYPE = 1;
    /**
     * sm2登录加密解密方式
     */
    public static final int SM2_LOGIN_TYPE = 2;
    /**
     * 前端加密前缀
     */
    public static final String ENCRYPTED_PREFIX = "04";

    /**
     * 加密算法
     */
    public static final String RSA = "rsa";
    public static final String SM2 = "sm2";
}
