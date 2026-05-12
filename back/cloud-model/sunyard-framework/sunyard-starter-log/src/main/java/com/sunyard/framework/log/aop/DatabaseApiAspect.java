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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.ip.IpUtils;
import com.sunyard.framework.log.annotation.ApiLog;
import com.sunyard.framework.log.constant.LogConstants;
import com.sunyard.module.system.api.LogApi;
import com.sunyard.module.system.api.UserApi;
import com.sunyard.module.system.api.dto.SysApiLogDTO;
import com.sunyard.module.system.api.dto.SysLogDTO;
import com.sunyard.module.system.api.dto.SysUserDTO;

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
public class DatabaseApiAspect extends BaseAspect {

    private static final ThreadLocal<Long> BEGIN_TIME_THREAD_LOCAL = new NamedThreadLocal<>(
            "DatabaseApi ThreadLocal beginTime");

    @Resource
    private Environment environment;
    @Resource
    private LogApi logApi;
    @Resource
    private UserApi userApi;

    /**
     * 对外接口日志
     */
    @Pointcut("@annotation(com.sunyard.framework.log.annotation.ApiLog)")
    public void pointCut() {
    }

    /**
     * 日志记录
     *
     * @param point 切入点
     * @return obj
     * @throws Throwable 异常
     */
    @Around(value = "pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        BEGIN_TIME_THREAD_LOCAL.set(System.nanoTime());
        String param = super.getRequestParams(point);
        super.inputConsoleLogs(point, LogConstants.STOREY_API, param);

        // 执行目标方法并获取返回值
        Object returnValue = point.proceed();

        long elapsedTimeInMillis = (System.nanoTime() - BEGIN_TIME_THREAD_LOCAL.get()) / 1000000;
        super.outputConsoleLogs(point, LogConstants.STOREY_API, null, elapsedTimeInMillis);

        int code = 0;

        // 判断返回值是否为 Result 类型
        if (returnValue instanceof Result) {
            Result result = (Result) returnValue;

            // 如果是 Result 类型，进行相应的处理
            code = ResultCode.SUCCESS.getCode().equals(result.getCode()) ? LogConstants.SUCCES
                    : LogConstants.FAIL;
        } else if (returnValue instanceof String) {
            //返回字符串暂默认成功
        } else {
            //别的暂时不处理
        }

        this.saveLog(param, point, code, null);
        return returnValue;
    }

    /**
     * 异常-日志记录
     *
     * @param exception 异常
     */
    @AfterThrowing(value = "pointCut()", throwing = "exception")
    public void doAfterThrowingAdvice(JoinPoint point, Throwable exception) {
        String param = super.getRequestParams(point);
        int responseCode = LogConstants.EXCEPTION;
        String mgs = SunyardException.printToStr(exception);
        if (exception instanceof SunyardException) {
            SunyardException e = (SunyardException) exception;
            if (e.getResultCode() == null) {
                responseCode = LogConstants.FAIL;
                mgs = exception.getMessage();
            } else if (!e.getResultCode().getCode()
                    .equals(ResultCode.SYSTEM_BUSY_ERROR.getCode())) {
                responseCode = LogConstants.FAIL;
                mgs = exception.getMessage();
            }
        } else if (exception instanceof IllegalArgumentException) {
            responseCode = LogConstants.FAIL;
            mgs = exception.getMessage();
        }
        this.saveLog(param, point, responseCode, mgs);
    }

    /**
     * 记录日志
     *
     * @param param 参数
     * @param point 切入点
     * @param code code
     * @param exMsg 异常信息
     */
    private void saveLog(String param, JoinPoint point, int code, String exMsg) {
        Profiles profiles = Profiles.of("local");
        boolean flag = environment.acceptsProfiles(profiles);
        if (flag) {
            return;
        }
        // 获取方法上的注解,判断是否需要写入日志
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        StringBuffer requestUrl = new StringBuffer();
        requestUrl.append(request.getScheme());
        requestUrl.append("://");
        requestUrl.append(request.getServerName());
        requestUrl.append(":");
        requestUrl.append(request.getServerPort());
        requestUrl.append(request.getRequestURI());

        SysLogDTO sysLog = new SysLogDTO();
        sysLog.setRequestIp(IpUtils.getClientIpAddr(request));
        sysLog.setRequestUrl(request.getRequestURI());
        ApiLog apiLog = method.getAnnotation(ApiLog.class);
        // 只记录异常或者 日志注解的日志
        if (null == apiLog) {
            if (!(code == LogConstants.EXCEPTION || code == LogConstants.FAIL)) {
                return;
            }
        }
        if (null != apiLog) {
            sysLog.setRequestDesc(StringUtils.hasText(apiLog.value()) ? apiLog.value() : "");
        }

        if (param != null && param.length() > LogConstants.OUTSIZE) {
            param = param.substring(0, LogConstants.OUTSIZE) + "...";
        }
        sysLog.setLogSystem(request.getHeader("serverName"));
        sysLog.setRequestParams(param);
        sysLog.setResponseCode(code);
        sysLog.setExceptionMsg(
                exMsg != null && exMsg.length() > 5000 ? exMsg.substring(0, 5000) : exMsg);
        sysLog.setCreateTime(new Date());

        String userId = request.getHeader("userInfo");
        if (userId != null) {
            try {
                userId = URLDecoder.decode(userId, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                log.error("系统异常", e);
                throw new RuntimeException(e);
            }
            JSONObject jsonObject = JSONObject.parseObject(userId);
            sysLog.setUserId(jsonObject.getLong("id"));
        }
        if (request.getRequestURI().startsWith(LogConstants.API)) {
            if ("3".equals(sysLog.getLogSystem()) && StringUtils.hasText(param)) {
                //影像系统对外接口入参有userCode记录
                try {
                    JSONObject jsonObject = JSONObject.parseObject(param);
                    JSONObject ecmBaseInfoDTO = jsonObject.getJSONObject("ecmBaseInfoDTO");
                    SysUserDTO sysUserDTO = new SysUserDTO();
                    sysUserDTO.setInstNo(ecmBaseInfoDTO.getString("orgCode"));
                    sysUserDTO.setRoleCodeList(
                            ecmBaseInfoDTO.getJSONArray("roleCode").toJavaList(String.class));
                    sysUserDTO.setLoginName(ecmBaseInfoDTO.getString("userCode"));
                    Result<SysUserDTO> sysUserDTOResult = userApi.checkUserSpecial(sysUserDTO);
                    sysLog.setUserId(sysUserDTOResult.getData().getUserId());
                } catch (Exception e) {
                    log.error("ApiLog获取用户ID失败:" , e);
                }
            }
            SysApiLogDTO sysApiLog = new SysApiLogDTO();
            BeanUtils.copyProperties(sysLog, sysApiLog);
            logApi.addSysApiLog(sysApiLog);
        } else {
            logApi.add(sysLog);
        }
    }

}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
