package com.sunyard.module.auth.api;
/*
 * Project: com.sunyard.am.controller
 *
 * File Created at 2021/7/2
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.util.AntPathMatcher;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.token.AccountToken;
import com.sunyard.framework.common.util.conversion.JsonUtils;
import com.sunyard.module.auth.api.dto.LoginUserInfoDTO;

/**
 * @author zhouleibin
 * @Type
 * @Desc 登录接口
 * @date 2021/7/2 14:31
 */
@RestController
public class GatewayApiImpl implements GatewayApi {
    @Resource
    private Map<String, String> filterChainDefinitionMap;

    @Override
    public Result<Map<String, String>> isPermitted(String token, String url) {
        // 是否认证登录
        Map<String, String> map = new HashMap<>(6);
        LoginUserInfoDTO dto = null;
        //user是否具有访问url的role
        String[] split = url.split("/");
        StringBuilder replace = new StringBuilder("/");
        //过滤掉 /web-api/这层，保留对应业务系统自身网关这一级，主要是为了区分不同系统相同路由的情况
        for (int i = 2; i < split.length; i++) {
            replace.append(split[i]).append("/");
        }
        if (replace.length() > 0) {
            replace.delete(replace.length() - 1, replace.length());
        }
        String filterChainValue = this.getFilterChainValue(replace.toString());
        boolean isPermitted = true;
        if (!ObjectUtils.isEmpty(filterChainValue)) {
            isPermitted = SecurityUtils.getSubject().hasRole(filterChainValue);
        }
        // 验证是否有角色权限
        AccountToken loginToken = (AccountToken)SecurityUtils.getSubject().getPrincipal();
        dto = new LoginUserInfoDTO();
        BeanUtils.copyProperties(loginToken, dto);
        if (!isPermitted) {
            map.put("ResultCode",
                JsonUtils.toJSONString(Result.error("缺少操作权限,请联系管理员配置", ResultCode.NO_OPERATION_AUTH)));
        }
        map.put("LoginUserInfoDTO", JsonUtils.toJSONString(dto));

        return Result.success(map);
    }

    /**
     * 根据url从filterChainDefinitionMap中拿到value
     *
     * @param url url
     * @return Result
     */
    public String getFilterChainValue(String url) {
        // 获取 filterChainDefinitionMap
        for (Map.Entry<String, String> entry : filterChainDefinitionMap.entrySet()) {
            String pattern = entry.getKey();
            String value = entry.getValue();
            // 使用 Shiro 自带的路径匹配工具进行 URL 匹配
            if (pathMatches(pattern, url) && value.contains("roles")) {
                return value.substring(value.indexOf("[") + 1, value.indexOf("]"));
            }
        }
        // 如果没有找到匹配的 URL，则返回 null 或者其他默认值
        return null;
    }

    /**
     * url匹配工具
     *
     * @param pattern 匹配格式
     * @param path 接口地址
     * @return Result
     */
    private boolean pathMatches(String pattern, String path) {
        // 使用 Shiro 自带的路径匹配工具进行 URL 匹配
        return new AntPathMatcher().matches(pattern, path);
    }
}

/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/2 zhouleibin creat
 */
