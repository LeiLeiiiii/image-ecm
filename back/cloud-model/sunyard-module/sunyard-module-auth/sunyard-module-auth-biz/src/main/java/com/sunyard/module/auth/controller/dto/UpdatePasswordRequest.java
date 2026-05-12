package com.sunyard.module.auth.controller.dto;

import lombok.Data;

/**
 * 三井密码同步请求参数
 */
@Data
public class UpdatePasswordRequest {

    /** 应用ID */
    private String appId;

    /** 应用密钥 */
    private String appSecret;

    /** 用户ID（loginName） */
    private String userId;

    /** 加密密码 */
    private String password;
}
