package com.sunyard.module.auth.controller;
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

import com.sunyard.framework.common.exception.ApiException;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.token.AccountToken;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.Enumeration;

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

    /**
     * 获取登录用户对象
     * @return AccountToken
     */
    protected AccountToken getToken() {
        Enumeration<String> headers = request.getHeaders("userId");
        return (AccountToken)SecurityUtils.getSubject().getPrincipal();
    }

    /**
     * 获取SessionId
     * @return String
     */
    protected String getSessionId() {
        Cookie[] cookies = request.getCookies();
        String sessionId = null;
        if (!ObjectUtils.isEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                switch (cookie.getName()) {
                    case "cloud-TOKEN":
                        sessionId = cookie.getValue();
                        break;
                    default:
                        break;
                }
            }
        }
        if (null == sessionId) {
            sessionId = SecurityUtils.getSubject().getSession().getId().toString();
        }
        return sessionId;
    }

    /**
     * Controller 异常捕获处理
     * @param exception 异常
     * @return Result
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
        } else if (exception instanceof RetryableException || exception instanceof ConnectException) {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            String msg = String.format("系统内部服务调用超时:{%s}", exception.getMessage());
            return Result.error(msg, ResultCode.SYSTEM_BUSY_ERROR);
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
