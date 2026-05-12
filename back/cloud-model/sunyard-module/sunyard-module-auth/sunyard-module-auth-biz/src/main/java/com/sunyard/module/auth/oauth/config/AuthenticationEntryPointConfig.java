package com.sunyard.module.auth.oauth.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.module.auth.oauth.response.ResponseCodeEnum;
import com.sunyard.module.auth.oauth.response.Result;

/**
 * @author P-JWei
 * @date 2023/11/23 14:38:53
 * @title
 * @description
 */
@Component
public class AuthenticationEntryPointConfig extends OAuth2AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        //走到此处说明token时效性过了。所以需要去redis删除对应得token键值对
        response.setContentType(ContentType.APPLICATION_JSON.toString());
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.println(JSONObject.toJSON(Result.error(ResponseCodeEnum.FAIL_TOKEN)));
        writer.flush();
    }
}
