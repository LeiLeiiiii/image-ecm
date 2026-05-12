package com.sunyard.module.auth.shiro.filter;
/*
 * Project: com.sunyard.am.shiro.filter
 *
 * File Created at 2021/7/2
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.conversion.JsonUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.shiro.filter
 * @Desc
 * @date 2021/7/2 13:35
 */
public class RolesFilter extends RolesAuthorizationFilter {

    @Override
    public boolean onPreHandle(ServletRequest servletRequest, ServletResponse servletResponse, Object mappedValue)
        throws Exception {
        if (!SecurityUtils.getSubject().isAuthenticated()) {
            printToPage(servletResponse, Result.error("登录状态已失效，请重新登录", ResultCode.NO_LOGIN_AUTH));
            return false;
        }
        if (!super.isAccessAllowed(servletRequest, servletResponse, mappedValue)) {
            printToPage(servletResponse, Result.error("缺少操作权限,请联系管理员配置", ResultCode.NO_OPERATION_AUTH));
            return false;
        }
        return true;

    }

    /**
     * 返回result
     * @param servletResponse 响应头
     * @param result result
     * @throws IOException io异常
     */
    private void printToPage(ServletResponse servletResponse, Result result) throws IOException {
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(JsonUtils.toJSONString(result));
        writer.flush();
        writer.close();
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/2 zhouleibin creat
 */
