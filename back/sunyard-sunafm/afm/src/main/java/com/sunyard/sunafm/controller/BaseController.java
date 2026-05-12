package com.sunyard.sunafm.controller;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.enums.LoginTypeEnum;
import com.sunyard.framework.common.exception.ApiException;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysUserDTO;
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
    private UserApi userApi;
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
//        runType = "local";
//        username = "rao1";
        if (LoginTypeEnum.LOCAL.getValue().equals(runType)) {
            SysUserDTO user = userApi.getUserDetail(username).getData();
            //模拟用户
            AccountToken loginToken = new AccountToken();
            loginToken.setId(user.getUserId());
            loginToken.setDeptId(user.getDeptId());
            loginToken.setInstId(user.getInstId());
            loginToken.setLoginType(user.getType());
            loginToken.setName(user.getName());
            loginToken.setOrgCode("23");
            loginToken.setOrgName("测试部门");
            loginToken.setName(user.getName());
            loginToken.setUsername(user.getLoginName());
            return loginToken;
        } else {
            String userId = request.getHeader("userInfo");
            try {
                userId = URLDecoder.decode(userId, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            if (userId != null) {
                JSONObject jsonObject = JSONObject.parseObject(userId);
                AccountToken loginToken = new AccountToken();
                loginToken.setId(jsonObject.getLong("id"));
                loginToken.setDeptId(jsonObject.getLong("deptId"));
                loginToken.setInstId(jsonObject.getLong("instId"));
                loginToken.setLoginType(jsonObject.getInteger("loginType"));
                try {
                    String decode = URLDecoder.decode(jsonObject.getString("name"),
                            StandardCharsets.UTF_8.toString());
                    loginToken.setName(decode);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                loginToken.setUsername(jsonObject.getString("username"));
                loginToken.setRememberMe(
                        !ObjectUtils.isEmpty(jsonObject.getBoolean("rememberMe")) && jsonObject.getBoolean("rememberMe"));
                loginToken.setHost(jsonObject.getString("host"));
                return loginToken;
            }
        }
        return null;
    }

    /**
     * Controller 异常捕获处理
     */
    @ExceptionHandler
    public Result exception(Exception exception) {
        if (exception instanceof SunyardException) {
            SunyardException sunyardException = (SunyardException) exception;
            return Result.error(sunyardException.getMessage(), sunyardException.getResultCode());
        } else if (exception instanceof IllegalArgumentException) {
            return Result.error(exception.getMessage(), ResultCode.PARAM_ERROR);
        } else if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            return Result.error(apiException.getMsg(), apiException.getCode());
            //以下是需要打印印象信息的
        } else if (exception instanceof DataAccessException || exception instanceof SQLException) {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            return Result.error("数据库异常", ResultCode.SYSTEM_ERROR);
        } else if (exception instanceof RuntimeException) {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            return Result.error(exception.getMessage(), ResultCode.SYSTEM_ERROR);
        } else {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            return Result.error(exception.toString(), ResultCode.SYSTEM_ERROR);
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
