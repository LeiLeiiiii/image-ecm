package com.sunyard.ecm.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author lw
 * @Description: 对外接口拦截器
 * @Date: 2023/5/23
 */
@Slf4j
public class OpenApiInterceptor implements HandlerInterceptor {

    /**
     * 请求前拦截
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //从请求头中获取所需参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        //1、授权认证
        //2、验证请求凭证是否过期、10分钟有效时间
        //3、验证签名
        return true;
    }

    /**
     * 方法处理
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        log.info("controller 执行完了");
    }

    /**
     * 拦截后操作
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("请求结束了");
    }

}
