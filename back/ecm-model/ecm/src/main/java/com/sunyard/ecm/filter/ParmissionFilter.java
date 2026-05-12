//package com.sunyard.ecm.filter;
//
//
//import com.alibaba.fastjson.JSONObject;
//import com.sunyard.ecm.config.NotAuthUrlPropertiesConfig;
//import com.sunyard.framework.common.enums.LoginLocalEnum;
//import com.sunyard.framework.common.result.Result;
//import com.sunyard.framework.common.result.ResultCode;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.util.PathMatcher;
//
//import javax.annotation.Resource;
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//
///**
// * @author 饶昌妹
// * @since 2023/7/31 10:19
// * @desc 权限过滤器
// */
//@Component
//@WebFilter("/*")
//public class ParmissionFilter implements Filter {
//    @Resource
//    private NotAuthUrlPropertiesConfig notAuthUrlPropertiesConfig;
//
//    @Value("${run.type:remote}")
//    private String runType;
//
//    @Value("${server.servlet.context-path:/web-api}")
//    private String contextPath;
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//
//        HttpServletRequest req = (HttpServletRequest) servletRequest;
//        HttpServletResponse response = (HttpServletResponse) servletResponse;
//        String path = contextPath + req.getServletPath();
//        if(path.startsWith(contextPath+"/druid")){
//            //德鲁伊
//            filterChain.doFilter(servletRequest, servletResponse);
//        }else if(path.startsWith(contextPath+"/actuator")){
//            //心跳-admin
//            filterChain.doFilter(servletRequest, servletResponse);
//        }else if (shouldSkip(path)) {
//            //若servletPath不以/user开头，那么访问的资源不需要登录即可访问
//            filterChain.doFilter(servletRequest, servletResponse);
//        } else {
//            if (LoginLocalEnum.LOCAL.getValue().equals(this.runType)) {
//                //本地模式-不需要校验权限
//                filterChain.doFilter(servletRequest, servletResponse);
//            } else if (LoginLocalEnum.LOCAL_USER.getValue().equals(this.runType)) {
//                //本地模式-不需要校验权限
//                filterChain.doFilter(servletRequest, servletResponse);
//            } else {
//                String userId = req.getHeader("userInfo");
//                if (userId != null) {
//                    filterChain.doFilter(servletRequest, servletResponse);
//                } else {
//                    getNoAuthRes(response);
//                    return;
//                }
//            }
//        }
//
//    }
//
//
//    /**
//     * 无权限的返回
//     *
//     * @param response
//     * @return
//     */
//    private void getNoAuthRes(HttpServletResponse response) throws IOException {
//        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(Result.error("登录状态已失效，请重新登录", ResultCode.NO_LOGIN_AUTH)));
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        //指定编码，否则在浏览器中会中文乱码
//        response.setHeader("Content-Type", "text/plain;charset=UTF-8");
//        PrintWriter writer = response.getWriter();
//        writer.print(jsonObject.toJSONString());
//        writer.flush();
//    }
//
//
//    /**
//     * 方法实现说明:不需要过滤的路径
//     * <p>
//     * //     * @param currentUrl 当前请求路径
//     */
//    private boolean shouldSkip(String currentUrl) {
//        PathMatcher pathMatcher = new AntPathMatcher();
//        for (String skipPath : notAuthUrlPropertiesConfig.getShouldSkipUrls()) {
//            if (pathMatcher.match(skipPath, currentUrl)) {
//                return true;
//            }
//        }
//        return false;
//    }
//}