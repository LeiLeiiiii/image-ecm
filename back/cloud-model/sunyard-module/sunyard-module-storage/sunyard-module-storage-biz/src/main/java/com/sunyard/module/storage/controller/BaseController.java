package com.sunyard.module.storage.controller;
/* Project: com.sunyard.am.controller
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license.
 */

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.enums.LoginTypeEnum;
import com.sunyard.framework.common.exception.ApiException;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.token.AccountToken;
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
 * 获取token
 *
 * @author zhouleibin
 * @Type com.sunyard.am.controller
 * @Desc
 * @since 2021/6/30 8:15
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
     * 运行的类型：local本地，remote
     */
    @Value("${run.type:remote}")
    private String runType;
    /**
     * 本地模式，默认用户
     */
    @Value("${run.name:admin}")
    private String username;

    /**
     * 获取登录用户对象
     */
    protected AccountToken getToken() {
        if (LoginTypeEnum.LOCAL.getValue().equals(runType)) {
            JSONObject jsonObject = JSONObject.parseObject(username);
            // 模拟用户
            AccountToken loginToken = new AccountToken();
            loginToken.setId(jsonObject.getLong("id"));
            loginToken.setDeptId(jsonObject.getLong("deptId"));
            loginToken.setInstId(jsonObject.getLong("instId"));
            loginToken.setLoginType(jsonObject.getInteger("loginType"));
            loginToken.setName(jsonObject.getString("name"));
            loginToken.setUsername(jsonObject.getString("username"));
            return loginToken;
        } else {
            String userId = request.getHeader("userInfo");
            if (userId != null) {
                try {
                    userId = URLDecoder.decode(userId, StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    log.error("获取登录用户对象失败",e);
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
                loginToken.setRememberMe(
                        ObjectUtils.isEmpty(jsonObject.getBoolean("rememberMe")) ? false
                                : jsonObject.getBoolean("rememberMe"));
                loginToken.setHost(jsonObject.getString("host"));
                return loginToken;
            }else{
                AccountToken loginToken = new AccountToken();
                loginToken.setOut(true);
                loginToken.setUsername(request.getHeader("usercode"));
                loginToken.setOrgCode(request.getHeader("orgCode"));
                loginToken.setName(request.getHeader("username"));
                loginToken.setRoleCode(request.getHeader("roleCode"));
                loginToken.setOrgName(request.getHeader("orgName"));
                return loginToken;
            }
        }
    }

    /**
     * Controller 异常捕获处理
     * @param exception 异常
     * @return Result
     */
    @ExceptionHandler
    public Result exception(Exception exception) {
        if (exception instanceof SunyardException) {
            SunyardException sunAmException = (SunyardException) exception;
            return Result.error(sunAmException.getMessage(), sunAmException.getResultCode());
        } else if (exception instanceof IllegalArgumentException) {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            return Result.error(exception.toString(), ResultCode.PARAM_ERROR);
        } else if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            return Result.error(apiException.getMsg(), apiException.getCode());
            //以下是需要打印印象信息的
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
 * Revision history
 * -------------------------------------------------------------------------
 * <p>
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2021/6/30 zhouleibin creat
 */
