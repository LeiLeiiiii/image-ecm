package com.sunyard.module.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author zhouleibin
 * @Desc 单点登录配置类
 * @date 2025/7/2 13:33
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties("auth.cas")
public class AuthSsoCasProperties {
    /** 单点验证服务器 */
    private String serverUrl;
    /** 单点成功后回调的页面地址，也就是我们自己的前端单点专用地址，前端跳转给客户页面带的地址必须和后端校验带的地址一致，否则后端校验会失败 */
    private String callbackUrl;
    /** 前端到单点带两个参数，一个是appid，客户给开的授权账号，另一个就是回调我们的单点登录地址 */
    private String appId;

}
