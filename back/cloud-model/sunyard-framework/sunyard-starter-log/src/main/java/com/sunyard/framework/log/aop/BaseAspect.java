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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import com.sunyard.framework.common.util.conversion.JsonUtils;
import com.sunyard.framework.log.annotation.CloseLog;
import com.sunyard.framework.log.constant.LogConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.aspect
 * @Desc
 * @date 2021/6/30 9:27
 */
@Slf4j
public class BaseAspect {
    @Resource
    protected HttpServletRequest request;

    /**
     * 日志输出
     *
     * @param point 切入点
     * @param storeyName storeyName
     * @param param 参数
     */
    protected void inputConsoleLogs(JoinPoint point, String storeyName, String param) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        CloseLog closeLogClass = AnnotatedElementUtils
                .findMergedAnnotation(point.getSignature().getDeclaringType(), CloseLog.class);
        CloseLog closeLogMethod = method.getAnnotation(CloseLog.class);

        if (ObjectUtils.isEmpty(closeLogMethod) && ObjectUtils.isEmpty(closeLogClass)) {
            if (log.isDebugEnabled()) {
                String className = point.getSignature().getDeclaringType().getSimpleName();
                String methodName = point.getSignature().getName();
                if (StringUtils.hasText(param)) {
                    log.debug(String.format("%s [%s %s.%s]%nParameters:[%s]", LogConstants.IN,
                            storeyName, className, methodName, param));
                } else {
                    log.debug(String.format("%s [%s %s.%s]", LogConstants.IN, storeyName, className,
                            methodName));
                }
            }
        }
    }

    /**
     * 日志输出
     * 
     * @param point 切入点
     * @param storeyName storeyName
     * @param param 参数
     */
    protected void outputConsoleLogs(JoinPoint point, String storeyName, String param,
                                     Long elapsedTimeInMillis) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        CloseLog closeLogClass = AnnotatedElementUtils
                .findMergedAnnotation(point.getSignature().getDeclaringType(), CloseLog.class);
        CloseLog closeLogMethod = method.getAnnotation(CloseLog.class);

        if (ObjectUtils.isEmpty(closeLogMethod) && ObjectUtils.isEmpty(closeLogClass)) {
            if (log.isDebugEnabled()) {
                String className = point.getSignature().getDeclaringType().getSimpleName();
                String methodName = point.getSignature().getName();
                if (StringUtils.hasText(param)) {
                    log.debug(String.format("%s [%s %s.%s] [%s ms]%nParameters:[%s]",
                            LogConstants.OUT, storeyName, className, methodName,
                            elapsedTimeInMillis, param));
                } else {
                    log.debug(String.format("%s [%s %s.%s] [%s ms]", LogConstants.OUT, storeyName,
                            className, methodName, elapsedTimeInMillis));
                }
            }
        }
    }

    /**
     * 获取入参
     * @param point 切入点
     * @return
     */
    protected String getRequestParams(JoinPoint point) {
        // 普通请求入参
        Map<String, String[]> requestMap = new HashMap(request.getParameterMap());
        requestMap.remove("timestamp");
        requestMap.remove("sign");
        if (requestMap.size() > 0) {
            return JsonUtils.toJSONString(requestMap);
        }
        Object[] args = point.getArgs();
        if (ObjectUtils.isEmpty(args)) {
            return "";
        }
        StringBuffer params = new StringBuffer();
        Object obj = args[0];
        // 文件请求
        if (obj instanceof StandardMultipartHttpServletRequest) {
            StandardMultipartHttpServletRequest standardMultipartHttpServletRequest = (StandardMultipartHttpServletRequest) obj;
            MultiValueMap<String, MultipartFile> multiValueMap = standardMultipartHttpServletRequest
                    .getMultiFileMap();
            Map<String, Object> map = new HashMap<String, Object>(6);
            for (String key : multiValueMap.keySet()) {
                List<Object> fileNameList = new ArrayList<>();
                for (MultipartFile multipartFile : multiValueMap.get(key)) {
                    Map<String, Object> fileMap = new HashMap<String, Object>(2);
                    fileMap.put("name", multipartFile.getOriginalFilename());
                    fileMap.put("size", multipartFile.getSize());
                    fileNameList.add(fileMap);
                }
                map.put(key, fileNameList);
            }
            params.append(JsonUtils.toJSONString(map));
        } else if (obj instanceof MultipartFile[]) {
            List<Object> fileNameList = new ArrayList<>();
            for (MultipartFile multipartFile : (MultipartFile[]) obj) {
                Map<String, Object> fileMap = new HashMap<String, Object>(2);
                fileMap.put("name", multipartFile.getOriginalFilename());
                fileMap.put("size", multipartFile.getSize());
                fileNameList.add(fileMap);
            }
            params.append(JsonUtils.toJSONString(fileNameList));
        } else if (!(obj instanceof ServletRequest) && !(obj instanceof ServletResponse)
                && !(obj instanceof MultipartFile)) {
            // json body请求
            try {
                String argString = JsonUtils.toJSONString(obj);
                params.append(argString);
            } catch (Exception e) {
                log.error("系统异常", e);
            }
        }
        return params.toString();
    }

}
