package com.sunyard.gateway.filter.auth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.core.NamedThreadLocal;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.conversion.JsonUtils;
import com.sunyard.gateway.config.WhiteListProperties;
import com.sunyard.gateway.util.ApplicationContextUtils;
import com.sunyard.module.auth.api.GatewayApi;
import com.sunyard.module.auth.api.dto.LoginUserInfoDTO;
import com.sunyard.module.auth.constant.TokenConstants;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author Leo
 * @Desc 鉴权过滤
 * @date 2023/5/11 11:13
 */
@Slf4j
@Order(1)
@Component
public class AuthFilter implements WebFilter {

    @Resource
    private WhiteListProperties whiteListDTO;

    private static final ThreadLocal<Long> BEGIN_TIME_THREAD_LOCAL =
        new NamedThreadLocal<>("AuthFilter ThreadLocal beginTime");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().value();
        // 对外的接口放掉，走下一个api认证过滤器
        if (requestPath.matches("^/web-api/[^/]+/api/.*$") || requestPath.matches("^/web-api/[^/]+/sign/.*$")) {
            return chain.filter(exchange);
        }
        try {
            BEGIN_TIME_THREAD_LOCAL.set(System.nanoTime());
            // 校验白名单（无须校验）
            if (this.checkWhitesList(requestPath)) {
                return chain.filter(exchange);
            }
            GatewayApi gatewayApi = ApplicationContextUtils.getBean(GatewayApi.class);
            Map<String, String> map = gatewayApi.isPermitted(this.getToken(exchange), requestPath).getData();

            LoginUserInfoDTO dto = null;
            Result resultCode = null;
            if (map != null) {
                dto = JsonUtils.parseObject(map.get("LoginUserInfoDTO"), LoginUserInfoDTO.class);
                resultCode = JsonUtils.parseObject(map.get("ResultCode"), Result.class);
            }
            if (dto != null) {
                // 角色权限
                if (resultCode != null) {
                    ServerHttpResponse response = exchange.getResponse();
                    JSONObject jsonObject = JsonUtils.parseObject(resultCode);
                    byte[] bits = JsonUtils.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8);
                    DataBuffer buffer = response.bufferFactory().wrap(bits);
                    response.setStatusCode(HttpStatus.PAYMENT_REQUIRED);
                    // 指定编码，否则在浏览器中会中文乱码
                    return response.writeWith(Mono.just(buffer));
                }
                String userInfo = JSONObject.toJSONString(dto);
                userInfo = URLEncoder.encode(userInfo, StandardCharsets.UTF_8.toString());
                ServerHttpRequest host = exchange.getRequest().mutate().header("userInfo", userInfo).build();
                ServerWebExchange build = exchange.mutate().request(host).build();
                return chain.filter(build);
            } else {
                ServerHttpResponse response = exchange.getResponse();
                JSONObject jsonObject = JsonUtils.parseObject(Result.error("登录状态已失效，请重新登录", ResultCode.NO_LOGIN_AUTH));
                byte[] bits = JsonUtils.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8);
                DataBuffer buffer = response.bufferFactory().wrap(bits);
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                // 指定编码，否则在浏览器中会中文乱码
                return response.writeWith(Mono.just(buffer));
            }
        } catch (UnsupportedEncodingException e) {
            log.error("系统异常", e);
            throw new RuntimeException(e);
        } finally {
            long beginTime = BEGIN_TIME_THREAD_LOCAL.get();
            long endTime = System.nanoTime();
            long elapsedTimeInMillis = (endTime - beginTime) / 1000000;
            BEGIN_TIME_THREAD_LOCAL.remove();
            PathMatcher pathMatcher = new AntPathMatcher();
            if (!pathMatcher.match("/web-api/actuator/**", requestPath)) {
                log.debug("网关转发:[地址:{},耗时:{}ms]", exchange.getRequest().getURI(), elapsedTimeInMillis);
            }
        }

    }

    /**
     * 校验白名单
     *
     * @param requestPath 请求路径
     * @return boolean
     */
    private boolean checkWhitesList(String requestPath) {
        // 获取请求微服务的请求路径
        PathMatcher pathMatcher = new AntPathMatcher();
        for (String path : whiteListDTO.getWhiteList()) {
            if (pathMatcher.match(path, requestPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从cookie中获取token
     *
     * @param exchange ServerWebExchange
     * @return token
     */
    private String getToken(ServerWebExchange exchange) {
        String token = "";
        ServerHttpRequest request = exchange.getRequest();
        if (null != request.getHeaders().get("Upgrade")
            && "websocket".equalsIgnoreCase(request.getHeaders().get("Upgrade").get(0))) {
            if (null != request.getQueryParams().get("token")) {
                token = request.getQueryParams().get("token").get(0);
            }
        } else {
            List<String> cookies = request.getHeaders().get("cookie");
            if (!CollectionUtils.isEmpty(cookies)) {
                token = cookies.get(0);
                String[] split1 = token.split(";");
                if (split1.length > 0) {
                    for (String s1 : split1) {
                        if (s1.contains(TokenConstants.SUNYARD_TOKEN)) {
                            String[] split = s1.split("=");
                            token = split[1];
                            break;
                        }
                    }
                }
            }
        }
        if(!StringUtils.hasText(token)){
            if (null != request.getQueryParams().get("token")) {
                token = request.getQueryParams().get("token").get(0);
            }
        }
        return token;
    }
}
