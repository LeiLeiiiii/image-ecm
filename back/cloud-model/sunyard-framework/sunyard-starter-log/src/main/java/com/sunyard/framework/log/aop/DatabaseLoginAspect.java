package com.sunyard.framework.log.aop;
/*
 * Project: com.sunyard.am.aspect
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.constant.LoginEncryptionConstant;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.encryption.RsaUtils;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import com.sunyard.framework.common.util.ip.IpUtils;
import com.sunyard.framework.log.constant.LogConstants;
import com.sunyard.module.system.api.LogApi;
import com.sunyard.module.system.api.dto.SysLogLoginDTO;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.aspect
 * @Desc
 * @date 2021/6/30 9:27
 */
@Slf4j
@Aspect
@Order(1)
@Component
public class DatabaseLoginAspect extends BaseAspect {
    private static final ThreadLocal<Long> BEGIN_TIME_THREAD_LOCAL = new NamedThreadLocal<>(
            "DatabaseLogin ThreadLocal beginTime");
    @Resource
    private Environment environment;
    @Resource
    private LogApi logApi;

    /**
     * 登录日志
     */
    @Pointcut("@annotation(com.sunyard.framework.log.annotation.LoginLog)")
    public void pointCut() {
    }

    /**
     * 记录登录日志
     * 
     * @param point 切入点
     * @return obj
     * @throws Throwable 异常
     */
    @Around(value = "pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        String name = point.getSignature().getName();
        String param = super.getRequestParams(point);
        if (null == param) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(param);
        Map<String, Object> innerMap = jsonObject.getInnerMap();
        JSONArray jsonarray = JSONArray.parseArray(innerMap.get("username").toString());
        String username = jsonarray.get(0).toString();
        String msg = "";
        if ("login".equals(name)) {
            msg = "登录成功";
            Integer loginEncryption = 1;
            switch (loginEncryption) {
                case LoginEncryptionConstant.RSA_LOGIN_TYPE:
                    username = RsaUtils.decrypt(username);
                    break;
                default:
                    username = Sm2Util.decrypt(LoginEncryptionConstant.ENCRYPTED_PREFIX + username);
            }
        } else if ("logout".equals(name)) {
            username = null == username ? "" : username;
            msg = "退出成功";
        } else {
            return null;
        }
        BEGIN_TIME_THREAD_LOCAL.set(System.nanoTime());
        super.inputConsoleLogs(point, LogConstants.STOREY_API, param);
        Result result = (Result) point.proceed();
        long elapsedTimeInMillis = (System.nanoTime() - BEGIN_TIME_THREAD_LOCAL.get()) / 1000000;
        super.outputConsoleLogs(point, LogConstants.STOREY_API, null, elapsedTimeInMillis);
        int code = 0;
        if (null != result) {
            if (!ResultCode.SUCCESS.getCode().equals(result.getCode())) {
                code = LogConstants.FAIL;
                msg = result.getMsg();
            }
        }
        this.saveLog(username, code, msg);
        return result;
    }

    /**
     * 异常-日志记录
     *
     * @param exception
     */
    @AfterThrowing(value = "pointCut()", throwing = "exception")
    public void doAfterThrowingAdvice(JoinPoint point, Throwable exception) throws Throwable {
        String param = super.getRequestParams(point);
        JSONObject jsonObject = JSONObject.parseObject(param);
        JSONArray jsonarray = JSONArray.parseArray(jsonObject.get("username").toString());
        String username = jsonarray.get(0).toString();

        Integer loginEncryption = 1;
        switch (loginEncryption) {
            case LoginEncryptionConstant.RSA_LOGIN_TYPE:
                username = RsaUtils.decrypt(username);
                break;
            default:
                username = Sm2Util.decrypt(LoginEncryptionConstant.ENCRYPTED_PREFIX + username);
        }
        int responseCode = LogConstants.FAIL;
        String mgs = SunyardException.printToStr(exception);
        if (exception instanceof SunyardException) {
            SunyardException e = (SunyardException) exception;
            if (e.getResultCode() == null) {
                mgs = exception.getMessage();
            } else if (!e.getResultCode().getCode()
                    .equals(ResultCode.SYSTEM_BUSY_ERROR.getCode())) {
                mgs = exception.getMessage();
            }
        } else if (exception instanceof IllegalArgumentException) {
            mgs = exception.getMessage();
        }
        this.saveLog(username, responseCode, mgs);
    }

    /**
     * 报错日志
     * 
     * @param userName 有用户名
     * @param code code
     * @param exMsg 错误信息
     */
    private void saveLog(String userName, int code, String exMsg) {
        Profiles profiles = Profiles.of("local");
        boolean flag = environment.acceptsProfiles(profiles);
        if (flag) {
            return;
        }
        // 获取登录ip
        // 获取请求头的User-Agent，拿到登录浏览器、登录系统
        String userAgentStr = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        SysLogLoginDTO sysLogLoginDTO = new SysLogLoginDTO();
        sysLogLoginDTO.setUserName(userName);
        sysLogLoginDTO.setLoginIp(IpUtils.getClientIpAddr(request));
        sysLogLoginDTO.setLoginBrowser(userAgent.getBrowser().getName());
        sysLogLoginDTO.setLoginSystem(userAgent.getPlatform().getName());
        sysLogLoginDTO.setLoginStatus(code);
        sysLogLoginDTO.setLoginMsg(exMsg);
        sysLogLoginDTO.setLoginTime(new Date());
        logApi.addLogin(sysLogLoginDTO);
    }

}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
