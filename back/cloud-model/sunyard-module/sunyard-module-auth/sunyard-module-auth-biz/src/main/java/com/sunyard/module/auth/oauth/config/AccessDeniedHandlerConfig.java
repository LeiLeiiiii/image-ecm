package com.sunyard.module.auth.oauth.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.module.auth.oauth.response.ResponseCodeEnum;
import com.sunyard.module.auth.oauth.response.Result;

/**
 * @author P-JWei
 * @date 2023/11/23 15:48:54
 * @title
 * @description
 */
@Component
public class AccessDeniedHandlerConfig extends OAuth2AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType(ContentType.APPLICATION_JSON.toString());
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.println(JSONObject.toJSON(Result.error(ResponseCodeEnum.FAIL_TOKENNOAPI)));
        writer.flush();
    }
}
