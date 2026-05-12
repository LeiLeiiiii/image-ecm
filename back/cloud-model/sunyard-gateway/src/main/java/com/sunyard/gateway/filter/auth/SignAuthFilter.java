package com.sunyard.gateway.filter.auth;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.conversion.JsonUtils;
import com.sunyard.gateway.config.WhiteListProperties;
import com.sunyard.gateway.util.ApplicationContextUtils;
import com.sunyard.module.auth.api.dto.OpenAuthDTO;
import com.sunyard.module.auth.api.dto.SignAuthApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author P-JWei
 * @date 2023/8/29 15:49:57
 * @title API鉴权过滤
 * @description
 */
@Slf4j
@Order(2)
@Component
public class SignAuthFilter implements WebFilter {
    @Resource
    private WhiteListProperties whiteListDTO;
    private final ObjectMapper objectMapper;

    public SignAuthFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().value();
        //不是对外api，不走验证逻辑放掉
        if (!requestPath.matches("^/web-api/[^/]+/sign/.*$")) {
            return chain.filter(exchange);
        }

        // 校验对外白名单（无须校验）
        if (this.checkApiWhitesList(requestPath)) {
            return chain.filter(exchange);
        }
        PathMatcher pathMatcher = new AntPathMatcher();
        if (!pathMatcher.match("/web-api/actuator/**", requestPath)) {
            log.info(String.format("请求网关-outApi:%s", exchange.getRequest().getURI()));
        }
        MediaType contentType = exchange.getRequest().getHeaders().getContentType();
        
        // 对于不同的contentType，采用不同的方式获取请求体
        if (isaBoolean(contentType)) {
            // 处理表单数据、文件上传等类型
            if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                // 文件上传类型，不需要解析body内容
                return getVoidMono(exchange, chain, requestPath, null);
            } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
                // 处理application/x-www-form-urlencoded类型
                return DataBufferUtils.join(exchange.getRequest().getBody())
                        .switchIfEmpty(Mono.just(exchange.getResponse().bufferFactory().wrap(new byte[0])))
                        .flatMap(dataBuffer -> {
                            // 保留数据缓冲区用于后续使用
                            DataBufferUtils.retain(dataBuffer);
                            String param = null;
                            try {
                                // 读取请求体内容
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                param = new String(bytes, StandardCharsets.UTF_8);
                                // 去除参数前后的空格
                                param = this.removeParamWhitespaces(param, contentType);
                                
                                // 创建新的DataBuffer用于重写请求体
                                Flux<DataBuffer> cachedFlux = Flux.defer(() ->
                                        Flux.just(exchange.getResponse().bufferFactory().wrap(bytes))
                                );
                                
                                // 构建新的请求
                                ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                                    @Override
                                    public Flux<DataBuffer> getBody() {
                                        return cachedFlux;
                                    }
                                };
                                
                                // 构建新的exchange
                                ServerWebExchange mutatedExchange = exchange.mutate()
                                        .request(mutatedRequest)
                                        .build();
                                
                                return getVoidMono(mutatedExchange, chain, requestPath, param);
                            } finally {
                                // 释放原始资源
                                DataBufferUtils.release(dataBuffer);
                            }
                        });
            } else {
                // 其他类型，直接放行
                return getVoidMono(exchange, chain, requestPath, null);
            }
        } else if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            // 处理JSON类型请求体
            return DataBufferUtils.join(exchange.getRequest().getBody())
                    .switchIfEmpty(Mono.just(exchange.getResponse().bufferFactory().wrap(new byte[0])))
                    .flatMap(dataBuffer -> {
                        // 保留数据缓冲区用于后续使用
                        DataBufferUtils.retain(dataBuffer);
                        String param = null;
                        try {
                            // 读取请求体内容
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            param = new String(bytes, StandardCharsets.UTF_8);
                            // 去除参数前后的空格
                            param = removeParamWhitespaces(param, contentType);
                            
                            // 创建新的DataBuffer用于重写请求体
                            Flux<DataBuffer> cachedFlux = Flux.defer(() ->
                                    Flux.just(exchange.getResponse().bufferFactory().wrap(bytes))
                            );
                            
                            // 构建新的请求
                            ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return cachedFlux;
                                }
                            };
                            
                            // 构建新的exchange
                            ServerWebExchange mutatedExchange = exchange.mutate()
                                    .request(mutatedRequest)
                                    .build();
                            
                            return getVoidMono(mutatedExchange, chain, requestPath, param);
                        } finally {
                            // 释放原始资源
                            DataBufferUtils.release(dataBuffer);
                        }
                    });
        } else {
            ServerHttpResponse response = exchange.getResponse();
            JSONObject jsonObject = JsonUtils.parseObject(Result.error(
                    "请指定Content-type范围（none、multipart/form-data、application/x-www-form-urlencoded、application/json）",
                    ResultCode.NO_LOGIN_AUTH));
            byte[] bits = JsonUtils.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bits);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            // 指定编码，否则在浏览器中会中文乱码
            return response.writeWith(Mono.just(buffer));
        }
    }


    /**
     * 进行认证
     *
     * @param exchange    exchange
     * @param chain       chain
     * @param requestPath requestPath
     * @param param
     * @return Mono<Void>
     */
    private Mono<Void> getVoidMono(ServerWebExchange exchange, WebFilterChain chain, String requestPath, String param) {
        SignAuthApi signAuthApi = ApplicationContextUtils.getBean(SignAuthApi.class);
        OpenAuthDTO openAuthDTO = getOpenAuthDTO(exchange, requestPath, param);
        JSONObject jsonObject = signAuthApi.signAuthToApi(openAuthDTO);
        return handleResponse(exchange, chain, jsonObject);

    }
    // 新增辅助方法：复用响应处理逻辑（避免代码重复，无需改动原有逻辑）
    private Mono<Void> handleResponse(ServerWebExchange exchange, WebFilterChain chain, JSONObject jsonObject) {
        if ("00000".equals(jsonObject.getString("code"))) {
            return chain.filter(exchange);
        } else {
            ServerHttpResponse response = exchange.getResponse();
            byte[] bits = JsonUtils.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bits);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.writeWith(Mono.just(buffer));
        }
    }

    /**
     * 判断请求头的 contentType
     *
     * @param contentType contentType
     * @return boolean
     */
    private boolean isaBoolean(MediaType contentType) {
        return null == contentType
                || MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)
                || MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)
                || MediaType.APPLICATION_OCTET_STREAM.isCompatibleWith(contentType);
    }



    /**
     * 获取auth对象
     *
     * @param exchange    ServerWebExchange
     * @param requestPath 请求路径
     * @param paramMap    参数map
     * @return Result
     */
    private OpenAuthDTO getOpenAuthDTO(ServerWebExchange exchange, String requestPath, String paramMap) {
        OpenAuthDTO openAuthDTO = new OpenAuthDTO();
        openAuthDTO.setAppid(this.getAppId(exchange));
        openAuthDTO.setTimestamp(this.getTimestamp(exchange));
        openAuthDTO.setSign(this.getSign(exchange));
        openAuthDTO.setReferer(this.getReferer(exchange));
        openAuthDTO.setUrl(requestPath);
        if(paramMap!=null){
            openAuthDTO.setParam(paramMap);
        }else{
            openAuthDTO.setParam("");
        }

        return openAuthDTO;
    }

    /**
     * 拿到appid
     *
     * @param exchange ServerWebExchange
     * @return Result
     */
    private String getAppId(ServerWebExchange exchange) {
        String appid = "";
        try {
            appid = exchange.getRequest().getHeaders().get("appid").get(0);
        } catch (NullPointerException exception) {
            if (HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
                try {
                    appid = exchange.getRequest().getQueryParams().get("appid").get(0);
                } catch (NullPointerException e) {
                    AssertUtils.isTrue(!StringUtils.hasText(appid) && appid.isEmpty(),"请求头appid参数为空");
                    return appid;
                }
            }
            AssertUtils.isTrue(!StringUtils.hasText(appid) && appid.isEmpty(),"请求头appid参数为空");
            return appid;
        }
        return appid;
    }

    /**
     * 获取Timestamp
     *
     * @param exchange ServerWebExchange
     * @return Result
     */
    private String getTimestamp(ServerWebExchange exchange) {
        String timestamp = "";
        try {
            timestamp = exchange.getRequest().getHeaders().get("timestamp").get(0);
        } catch (NullPointerException exception) {
            if (HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
                try {
                    timestamp = exchange.getRequest().getQueryParams().get("timestamp").get(0);
                } catch (NullPointerException e) {
                    return timestamp;
                }
            }
            return timestamp;
        }
        return timestamp;
    }

    /**
     * 获取Sign
     *
     * @param exchange ServerWebExchange
     * @return Result
     */
    private String getSign(ServerWebExchange exchange) {
        String sign = "";
        try {
            sign = exchange.getRequest().getHeaders().get("sign").get(0);
        } catch (NullPointerException exception) {
            if (HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
                try {
                    sign = exchange.getRequest().getQueryParams().get("sign").get(0);
                } catch (NullPointerException e) {
                    return sign;
                }
            }
            return sign;
        }
        return sign;
    }

    /**
     * 获取referer
     *
     * @param exchange ServerWebExchange
     * @return Result
     */
    private String getReferer(ServerWebExchange exchange) {
        String referer = "";
        try {
            referer = exchange.getRequest().getHeaders().get("referer").get(0);
        } catch (NullPointerException exception) {
            return referer;
        }
        return referer;
    }

    /**
     * 校验url是否属性第三方认证
     *
     * @param requestPath 请求路径
     * @return Result
     */
    private boolean checkApiWhitesList(String requestPath) {
        // 获取请求微服务的请求路径
        PathMatcher pathMatcher = new AntPathMatcher();
        if (!CollectionUtils.isEmpty(whiteListDTO.getApiWhiteList())) {
            for (String path : whiteListDTO.getApiWhiteList()) {
                if (pathMatcher.match(path, requestPath)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 去除参数前后的空格
     * @param param 参数字符串
     * @param contentType 内容类型
     * @return 处理后的参数字符串
     */
    private String removeParamWhitespaces(String param, MediaType contentType) {
        if (param == null || param.isEmpty()) {
            return param;
        }
        
        try {
            if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                // 处理JSON参数，去除每个字段值前后的空格
                JsonNode jsonNode = objectMapper.readTree(param);
                if (jsonNode.isObject()) {
                    ObjectNode objectNode = (ObjectNode) jsonNode;
                    Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        JsonNode valueNode = field.getValue();
                        if (valueNode.isTextual()) {
                            // 去除字符串类型字段值的前后空格
                            objectNode.put(field.getKey(), valueNode.asText().trim());
                        }
                    }
                    return objectMapper.writeValueAsString(objectNode);
                }
            } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
                // 处理表单参数，去除每个参数值前后的空格
                Map<String, String> paramMap = new LinkedHashMap<>();
                String[] params = param.split("&");
                for (String paramPair : params) {
                    if (paramPair.contains("=")) {
                        String[] keyValue = paramPair.split("=", 2);
                        String key = java.net.URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                        String value = keyValue.length > 1 ? 
                            java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name()).trim() : "";
                        paramMap.put(key, value);
                    }
                }
                
                // 重新组装表单参数
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(java.net.URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()));
                    sb.append("=");
                    sb.append(java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()));
                }
                return sb.toString();
            }
        } catch (Exception e) {
            log.error("去除参数空格失败", e);
        }
        
        // 如果处理失败，返回原始参数
        return param;
    }

    //     根据请求类型和内容类型获取需要签名的数据
    private Mono<Object> getSignData(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        if (request.getMethod() == HttpMethod.GET) {
            // GET请求从查询参数获取数据（排除sign本身）
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            Map<String, String> signData = new TreeMap<>();
            queryParams.forEach((key, values) -> {
                if (!"sign".equals(key) && values != null && !values.isEmpty()) {
                    signData.put(key, values.get(0));
                }
            });
            return Mono.just(signData);
        } else {
            // POST请求根据内容类型处理
            MediaType contentType = request.getHeaders().getContentType();
            if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                // JSON类型请求体
                return DataBufferUtils.join(request.getBody())
                        .switchIfEmpty(Mono.error(new RuntimeException("json请求体不能为空内容")))
                        .flatMap(dataBuffer -> {
                            DataBufferUtils.retain(dataBuffer);
                            Flux<DataBuffer> cachedFlux = Flux.defer(() ->
                                    Flux.just(dataBuffer.slice(0, dataBuffer.readableByteCount()))
                            );

                            String requestBody = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer()).toString();
                            try {
                                JsonNode jsonNode = objectMapper.readTree(requestBody);
                                return Mono.just(new CachedBody(jsonNode, cachedFlux, dataBuffer));
                            } catch (Exception e) {
                                DataBufferUtils.release(dataBuffer);
                                return Mono.error(new RuntimeException("解析JSON请求体失败", e));
                            }
                        });
            } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
                // 处理表单编码类型 (application/x-www-form-urlencoded)，缓存请求体并提取签名数据
                return DataBufferUtils.join(request.getBody())
                        .switchIfEmpty(Mono.error(new RuntimeException("表单请求体不能为空")))
                        .flatMap(dataBuffer -> {
                            try {
                                //保留数据缓冲区用于缓存
                                DataBufferUtils.retain(dataBuffer);
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes); // 读取原始字节

                                //缓存请求体Flux（用于后续重建）
                                Flux<DataBuffer> cachedFlux = Flux.defer(() ->
                                        Flux.just(exchange.getResponse().bufferFactory().wrap(bytes))
                                );

                                //解析表单数据（application/x-www-form-urlencoded格式）
                                String formBody = new String(bytes, StandardCharsets.UTF_8);
                                Map<String, Object> signData = new TreeMap<>();
                                String[] params = formBody.split("&");
                                for (String param : params) {
                                    if (param.contains("=")) {
                                        String[] keyValue = param.split("=", 2); // 按第一个=分割
                                        String key = java.net.URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                                        String value = keyValue.length > 1 ?
                                                java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name()) : "";
                                        signData.put(key, value);
                                    }
                                }

                                //返回包含签名数据和缓存体的对象
                                return Mono.just(new MultipartCachedBody(signData, cachedFlux));
                            } catch (Exception e) {
                                return Mono.error(new RuntimeException("解析表单请求体失败", e));
                            }finally {
                                DataBufferUtils.release(dataBuffer);
                            }
                        });
            } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                return DataBufferUtils.join(exchange.getRequest().getBody())
                        .flatMap(dataBuffer -> {
                            try {
                                // 1. 保留数据缓冲区
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                dataBuffer = null; // 防止重复使用
                                // 2. 将原始body转为字符串便于解析
                                String bodyString = new String(bytes, StandardCharsets.UTF_8);

                                // 3. 提取boundary
                                String boundary = contentType.getParameter("boundary");
                                if (boundary == null) {
                                    boundary = extractBoundary(bodyString);
                                }

                                // 4. 创建结果Map
                                Map<String, Object> result = new TreeMap<>();

                                // 5. 解析multipart body提取参数
                                String[] parts = bodyString.split("--" + boundary);
                                for (String part : parts) {
                                    if (part.contains("Content-Disposition: form-data")) {
                                        // 提取字段名
                                        String name = extractValue(part, "name=\"([^\"]+)\"");

                                        // 跳过文件字段
                                        if (part.contains("filename=")) {
                                            continue;
                                        }

                                        // 提取字段值
                                        String value = part.substring(part.indexOf("\r\n\r\n") + 4)
                                                .replaceAll("\r\n--$", "").trim();

                                        if (name != null && !name.isEmpty()) {
                                            result.put(name, value);
                                        }
                                    }
                                }
                                // 7. 使用工厂方法创建新的DataBuffer，让框架管理生命周期
                                Flux<DataBuffer> newBody = Flux.just(
                                        exchange.getResponse().bufferFactory().wrap(bytes)
                                );
                                // 8. 返回结果
                                return Mono.just(new MultipartCachedBody(result, newBody));
                            } catch (Exception e){
                                DataBufferUtils.release(dataBuffer);
                                return Mono.error(new RuntimeException("请求体失败", e));
                            }
                        });
            } else if (MediaType.APPLICATION_OCTET_STREAM.isCompatibleWith(contentType)) {
                // application/octet-stream只取url参数做校验
                MultiValueMap<String, String> queryParams = request.getQueryParams();
                Map<String, String> signData = new TreeMap<>();
                queryParams.forEach((key, values) -> {
                    if (!"sign".equals(key) && values != null && !values.isEmpty()) {
                        signData.put(key, values.get(0));
                    }
                });
                return Mono.just(signData);
            } else {
                // 其余类型直接放行
                return Mono.just("success");
            }
        }
    }


    /**
     * 同步读取请求体（含超时控制+资源释放）
     */
    private byte[] readRequestBodySync(ServerHttpRequest request) throws Exception {
        // 最大请求体大小：1MB（可配置）
        long maxBodySize = 1024 * 1024;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        long totalBytes = 0;

        // 遍历 DataBuffer 流（手动读取，无阻塞，兼容低版本）
        for (Iterator<DataBuffer> iterator = request.getBody().toIterable().iterator(); iterator.hasNext(); ) {
            DataBuffer dataBuffer = iterator.next();
            try {
                int readableBytes = dataBuffer.readableByteCount();
                // 校验是否超过最大大小
                if (totalBytes + readableBytes > maxBodySize) {
                    throw new RuntimeException("请求体过大（超过1MB），不支持鉴权");
                }
                // 读取字节到输出流
                byte[] bytes = new byte[readableBytes];
                dataBuffer.read(bytes);
                outputStream.write(bytes);
                totalBytes += readableBytes;
            } finally {
                // 关键：手动释放 DataBuffer 资源，避免内存泄漏
                DataBufferUtils.release(dataBuffer);
            }
        }

        outputStream.flush();
        return outputStream.toByteArray();
    }


    /**
     * 构建错误响应（统一格式）
     */
    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        JSONObject errorJson = new JSONObject();
        errorJson.put("code", ResultCode.NO_LOGIN_AUTH.getCode()); // 替换为你的错误码枚举
        errorJson.put("msg", msg);
        errorJson.put("data", "");

        byte[] bits = errorJson.toJSONString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 校验支持的 Content-Type
     */
    private boolean isSupportedContentType(MediaType contentType) {
        if (contentType == null) {
            return true; // none 类型
        }
        return MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)
                || MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)
                || MediaType.APPLICATION_JSON.isCompatibleWith(contentType);
    }

    /**
     * 正则提取值（如 name="xxx"）
     */
    private String extractValue(String content, String regex) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


        // 缓存JSON请求体的内部类
    private static class CachedBody {
        private final JsonNode jsonNode;
        private final Flux<DataBuffer> cachedFlux;
        private final DataBuffer originalDataBuffer;

        public CachedBody(JsonNode jsonNode, Flux<DataBuffer> cachedFlux, DataBuffer originalDataBuffer) {
            this.jsonNode = jsonNode;
            this.cachedFlux = cachedFlux;
            this.originalDataBuffer = originalDataBuffer;
        }

        public JsonNode getJsonNode() {
            return jsonNode;
        }

        public Flux<DataBuffer> getCachedFlux() {
            return cachedFlux.doFinally(signalType -> {
                DataBufferUtils.release(originalDataBuffer);
            });
        }
    }

    private static class MultipartCachedBody {
        private final Map<String, Object> signData;
        private final Flux<DataBuffer> cachedFlux;

        public MultipartCachedBody(Map<String, Object> signData, Flux<DataBuffer> cachedFlux) {
            this.signData = signData;
            this.cachedFlux = cachedFlux;
        }

        public Map<String, Object> getSignData() {
            return signData;
        }

        public Flux<DataBuffer> getCachedFlux() {
            return cachedFlux;
        }
    }


    /**
     * 提取 Multipart boundary（从请求体字符串）
     */
    private String extractBoundary(String bodyString) {
        String[] lines = bodyString.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("--") && line.length() > 2) {
                return line.substring(2).trim();
            }
        }
        throw new RuntimeException("Multipart boundary 提取失败");
    }
}
