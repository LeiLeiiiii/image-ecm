package com.sunyard.module.system.controller;
/*
 * Project: com.sunyard.am.controller
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.enums.LoginLocalEnum;
import com.sunyard.framework.common.enums.LoginTypeEnum;
import com.sunyard.framework.common.exception.ApiException;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.module.system.api.dto.SysUserDTO;
import com.sunyard.module.system.service.OrgUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.controller
 * @Desc
 * @date 2021/6/30 8:15
 */
@Slf4j
@RestController
@RestControllerAdvice
public class BaseController {
    @Resource
    protected HttpServletRequest request;
    @Resource
    protected HttpServletResponse response;
    @Resource
    protected OrgUserService orgUserService;
    /** 运行的类型：local本地，remote */
    @Value("${run.type:remote}")
    private String runType;
    /**
     * 本地模式，默认用户
     */
    @Value("${run.name:admin}")
    private String username;

    /** 本地模式，默认用户 */
    @Value("${run.user:{ \"userId\": 3037957083554817}}")
    private String user;

    /**
     * 获取登录用户对象
     * @return Result 登录对象
     */
    protected AccountToken getToken() {
        if (LoginTypeEnum.LOCAL.getValue().equals(runType)) {
            SysUserDTO user = orgUserService.getUserDetail(username);
            // 模拟用户
            AccountToken loginToken = new AccountToken();
            loginToken.setId(user.getUserId());
            loginToken.setDeptId(user.getDeptId());
            loginToken.setInstId(user.getInstId());
            loginToken.setLoginType(user.getType());
            loginToken.setName(user.getName());
            loginToken.setUsername(user.getLoginName());
            return loginToken;
        } else if (LoginLocalEnum.LOCAL_USER.getValue().equals(runType)) {
            JSONObject jsonObject = JSONObject.parseObject(user);
            AccountToken loginToken = new AccountToken();
            loginToken.setId(jsonObject.getLong("userId"));
            loginToken.setDeptId(jsonObject.getLong("deptId"));
            loginToken.setInstId(jsonObject.getLong("instId"));
            loginToken.setLoginType(jsonObject.getInteger("type"));
            loginToken.setName(jsonObject.getString("name"));
            loginToken.setUsername(jsonObject.getString("loginName"));
            return loginToken;
        } else {
            String userId = request.getHeader("userInfo");
            if (userId != null) {
                try {
                    userId = URLDecoder.decode(userId, StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    log.error("系统异常", e);
                    throw new RuntimeException(e);
                }
                JSONObject jsonObject = JSONObject.parseObject(userId);
                AccountToken loginToken = new AccountToken();
                loginToken.setId(jsonObject.getLong("id"));
                loginToken.setDeptId(jsonObject.getLong("deptId"));
                loginToken.setInstId(jsonObject.getLong("instId"));
                loginToken.setLoginType(jsonObject.getInteger("loginType"));
                loginToken.setName(jsonObject.getString("name"));
                loginToken.setUsername(jsonObject.getString("username"));
                loginToken.setRememberMe(ObjectUtils.isEmpty(jsonObject.getBoolean("rememberMe")) ? false
                    : jsonObject.getBoolean("rememberMe"));
                loginToken.setHost(jsonObject.getString("host"));
                return loginToken;
            }
        }
        return null;
    }

    /**
     * 获取客户端cookie
     */
    protected String getCookie() {
        return request.getHeader("Cookie");
    }

    /**
     * Controller 异常捕获处理
     * @param exception  异常
     * @return result
     */
    @ExceptionHandler
    public Result exception(Exception exception) {
        if (exception instanceof SunyardException) {
            SunyardException sunyardException = (SunyardException)exception;
            return Result.error(sunyardException.getMessage(), sunyardException.getResultCode());
        } else if (exception instanceof IllegalArgumentException) {
            return Result.error(exception.getMessage(), ResultCode.PARAM_ERROR);
        } else if (exception instanceof ApiException) {
            ApiException apiException = (ApiException)exception;
            return Result.error(apiException.getMsg(), apiException.getCode());
            // 以下是需要打印印象信息的
        } else if (exception instanceof DataAccessException || exception instanceof SQLException) {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            return Result.error("数据库异常", ResultCode.SYSTEM_BUSY_ERROR);
        } else if (exception instanceof RuntimeException) {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            return Result.error(exception.getMessage(), ResultCode.SYSTEM_BUSY_ERROR);
        } else {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            return Result.error(exception.toString(), ResultCode.SYSTEM_BUSY_ERROR);
        }
    }

}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
