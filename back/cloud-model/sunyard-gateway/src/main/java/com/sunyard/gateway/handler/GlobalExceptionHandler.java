package com.sunyard.gateway.handler;
/*
 * Project: com.sunyard.am.controller
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.conversion.JsonUtils;

import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.controller
 * @Desc 保证优先级高于默认的 Spring Cloud Gateway 的 ErrorWebExceptionHandler 实现
 * @date 2021/6/30 8:15
 */
@Slf4j
@Order(-1)
@Configuration
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 已经 commit，则直接返回异常
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        Result result = defaultExceptionHandler(exchange, ex);
        return writeJson(exchange, result);
    }

    /**
     * 设置响应体
     * @param exchange ServerWebExchange
     * @param object object
     * @return result
     */
    public static Mono<Void> writeJson(ServerWebExchange exchange, Object object) {
        // 设置 header
        ServerHttpResponse response = exchange.getResponse();
        // 必须使用 APPLICATION_JSON_UTF8_VALUE，否则会乱码
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 设置 body
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            ServerHttpRequest request = exchange.getRequest();
            try {
                return bufferFactory.wrap(JsonUtils.toJSONString(object).getBytes(StandardCharsets.UTF_8));
            } catch (Exception ex) {
                log.error("[writeJSON][uri({}/{}) 发生异常]", request.getURI(), request.getMethod(), ex);
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }

    /**
     * 异常捕获
     * @param exchange ServerWebExchange
     * @param exception 异常
     * @return result
     */
    public Result defaultExceptionHandler(ServerWebExchange exchange, Throwable exception) {
        if (exception instanceof ResponseStatusException) {
            ResponseStatusException e = (ResponseStatusException)exception;
            ServerHttpRequest request = exchange.getRequest();
            String msg = String.format("请求地址不可达:{%s}", request.getURI());
            return Result.error(msg, e.getRawStatusCode());
        } else if (exception instanceof SunyardException) {
            SunyardException e = (SunyardException)exception;
            return Result.error(e.getMessage(), e.getResultCode());
        } else if (exception instanceof RetryableException || exception instanceof ConnectException) {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            String msg = String.format("系统服务正在发布，请耐心等待...");
            return Result.error(msg, ResultCode.SYSTEM_BUSY_ERROR);
        } else if (exception instanceof RuntimeException) {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            return Result.error(exception.getMessage(), ResultCode.SYSTEM_BUSY_ERROR);
        } else {
            log.error("执行失败 {}", SunyardException.printToStr(exception));
            return Result.error(exception.toString(), ResultCode.SYSTEM_BUSY_ERROR);
        }
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
