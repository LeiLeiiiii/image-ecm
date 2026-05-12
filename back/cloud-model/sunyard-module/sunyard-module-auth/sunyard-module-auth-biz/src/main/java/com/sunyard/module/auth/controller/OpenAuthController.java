package com.sunyard.module.auth.controller;

import com.sunyard.module.auth.oauth.response.Result;
import com.sunyard.module.auth.service.OpenAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author P-JWei
 * @date 2023/8/25 9:22:31
 * @title
 * @description
 */
@RestController
@RequestMapping
public class OpenAuthController {

    @Resource
    private OpenAuthService openAuthService;

    /**
     * 获取token
     *
     * @param appId     appId
     * @param appSecret appSecret
     * @return result
     */
    @PostMapping("getOutApiToken")
    public Result getToken(String appId, String appSecret) {
        return openAuthService.getAccessToken(appId, appSecret);
    }

    @PostMapping("getRefreshApiToken")
    public Result getRefreshApiToken(String appId, String appSecret,String refreshToken) {
        return openAuthService.getRefreshApiToken(appId, appSecret,refreshToken);
    }

}
