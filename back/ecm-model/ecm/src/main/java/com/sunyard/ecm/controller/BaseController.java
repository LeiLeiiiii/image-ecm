package com.sunyard.ecm.controller;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.lock.exception.LockFailureException;
import com.sunyard.ecm.dto.AccountTokenExtendDTO;
import com.sunyard.ecm.dto.redis.UserBusiRedisDTO;
import com.sunyard.ecm.manager.BusiCacheService;
import com.sunyard.framework.common.enums.LoginTypeEnum;
import com.sunyard.framework.common.exception.ApiException;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.module.auth.constant.TokenConstants;
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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * @author 饶昌妹
 * @since: 2023/8/14
 * @Desc: token拦截配置controller
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
    //运行的类型：local本地，remote
    @Value("${run.type:remote}")
    private String runType;
    //本地模式，默认用户
    @Value("${run.name:rao1}")
    private String username;
    @Resource
    private BusiCacheService busiCacheService;

    /**
     * 获取登录用户对象
     */
    protected AccountTokenExtendDTO getToken() {
        if (LoginTypeEnum.LOCAL.getValue().equals(runType)) {
            SysUserDTO user = userApi.getUserDetail(username).getData();
            //模拟用户
            AccountTokenExtendDTO loginToken = new AccountTokenExtendDTO();
            loginToken.setId(user.getUserId());
            loginToken.setDeptId(user.getDeptId());
            loginToken.setInstId(user.getInstId());
            loginToken.setLoginType(user.getType());
            loginToken.setName(user.getName());
            loginToken.setUsername(user.getLoginName());
            return loginToken;
        } else {
            String userId = request.getHeader("userInfo");

            String token = getToken(request);
            if (userId != null) {
                try {
                    userId = URLDecoder.decode(userId, StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                JSONObject jsonObject = JSONObject.parseObject(userId);
                AccountTokenExtendDTO loginToken = new AccountTokenExtendDTO();
                loginToken.setId(jsonObject.getLong("id"));
                loginToken.setDeptId(jsonObject.getLong("deptId"));
                loginToken.setInstId(jsonObject.getLong("instId"));
                loginToken.setOrgName(jsonObject.getString("orgName"));
                loginToken.setOrgCode(jsonObject.getString("orgCode"));
                if(jsonObject.get("roleIdList")!=null){
                    loginToken.setRoleIdList((List<Long>)jsonObject.get("roleIdList"));
                }
                if(jsonObject.get("roleCodeList")!=null) {
                    loginToken.setRoleCodeList((List<String>) jsonObject.get("roleCodeList"));
                }
                loginToken.setLoginType(jsonObject.getInteger("loginType"));
                loginToken.setName(jsonObject.getString("name"));
                loginToken.setUsername(jsonObject.getString("username"));
                loginToken.setRememberMe(
                        !ObjectUtils.isEmpty(jsonObject.getBoolean("rememberMe")) && jsonObject.getBoolean("rememberMe"));
                loginToken.setHost(jsonObject.getString("host"));
                String flagId = request.getHeader("flagId");
                loginToken.setFlagId(flagId);
                loginToken.setTokenValue(token);
                return loginToken;
            } else {
                String flagId = request.getHeader("flagId");
                UserBusiRedisDTO userBusiRedisDTO = busiCacheService.getUserPageRedis(flagId,null);
                AccountTokenExtendDTO loginToken = null;
                if (userBusiRedisDTO!=null) {
                    //新增只会新增一笔业务类型
                    loginToken = new AccountTokenExtendDTO();
                    loginToken.setUsername(userBusiRedisDTO.getUsercode());
                    loginToken.setName(userBusiRedisDTO.getUsername());
                    loginToken.setOrgCode(userBusiRedisDTO.getOrg());
                    loginToken.setOrgName(userBusiRedisDTO.getOrgName());
                    loginToken.setRoleCodeList(userBusiRedisDTO.getRole());
                    loginToken.setInstId(userBusiRedisDTO.getInstId());
                    loginToken.setRoleIdList(userBusiRedisDTO.getRoleIds());
                    loginToken.setFlagId(flagId);
                    loginToken.setOut(true);
                    loginToken.setIsShow(userBusiRedisDTO.getIsShow());

                }
                AssertUtils.isNull(loginToken, "当前用户不存在。");
                loginToken.setTokenValue(token);
                return loginToken;
            }
        }
    }

    /**
     * 从cookie中获取token
     */
    private String getToken(HttpServletRequest exchange) {
        Cookie[] cookies = exchange.getCookies();
        String token = "";
        if (ArrayUtil.isNotEmpty(cookies)) {
            for(Cookie cookie : cookies){
                if(TokenConstants.SUNYARD_TOKEN.equals(cookie.getName())){
                    token = cookie.getValue();
                    break;
                }
            }
        }

        return token;
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
        } else if (exception instanceof LockFailureException) {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            return Result.error("请勿重复操作", ResultCode.SYSTEM_ERROR);
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
