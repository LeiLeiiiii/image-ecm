package com.sunyard.gateway.filter.auth;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.common.result.ResultCode;
import com.sunyard.framework.common.util.conversion.JsonUtils;
import com.sunyard.gateway.config.WhiteListProperties;
import com.sunyard.gateway.util.ApplicationContextUtils;
import com.sunyard.module.auth.api.OpenAuthApi;
import com.sunyard.module.auth.api.dto.OpenAuthDTO;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author P-JWei
 * @date 2023/8/29 15:49:57
 * @title API鉴权过滤
 * @description
 */
@Slf4j
@Order(2)
@Component
public class ApiAuthFilter implements WebFilter {
    public static final String JWT_TOKEN = "jwtToken:";
    // 常量：存储缓存体的exchange属性key（本地使用，不跨服务）
    private static final String LOCAL_CACHED_BODY = "LOCAL_CACHED_BODY";
    @Resource
    private WhiteListProperties whiteListDTO;
    private final ObjectMapper objectMapper;

    public ApiAuthFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().value();
        //不是对外api，不走验证逻辑放掉
        if (!requestPath.matches("^/web-api/[^/]+/api/.*$")) {
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
        String param = null;
        if (isaBoolean(contentType)) {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            List<String> strings = headers.get("bodyData");
            if(!CollectionUtils.isEmpty(strings)){
                param = strings.get(0);
            }

            return getVoidMono(exchange, chain, requestPath,param);
        } else if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            List<String> strings = headers.get("bodyData");
            if(!CollectionUtils.isEmpty(strings)){
                param = strings.get(0);
            }
            return getVoidMono(exchange, chain, requestPath,param);
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
        OpenAuthApi openAuthApi = ApplicationContextUtils.getBean(OpenAuthApi.class);
//        JSONObject jsonObject = openAuthApi.openAuthToApi(getOpenAuthDTO(exchange, requestPath, param));
        OpenAuthDTO openAuthDTO = getOpenAuthDTO(exchange, requestPath, param);
        if(openAuthDTO.getToken().startsWith(JWT_TOKEN)){
            openAuthDTO.setToken(openAuthDTO.getToken().replace(JWT_TOKEN, ""));
            return getSignData(exchange)
                    .flatMap(signData -> { // 订阅后获取 Object 类型 signData
                        try {
                            JSONObject jsonObject = null;
                            // 拆分签名数据和缓存体
                            Object signDataForAuth; // 跨服务传递的签名数据（基础类型）
                            if (signData instanceof CachedBody) {
                                // JSON场景：仅传递 JsonNode 转成的 Map（跨服务可序列化）
                                signDataForAuth = objectMapper.convertValue(((CachedBody) signData).getJsonNode(), Map.class);
                            } else if (signData instanceof MultipartCachedBody) {
                                // 表单/多部分场景：仅传递 signData Map（跨服务可序列化）
                                signDataForAuth = ((MultipartCachedBody) signData).getSignData();
                            } else {
                                // GET/二进制流场景：直接传递 Map
                                signDataForAuth = signData;
                            }

                            //：DTO中仅设置可序列化的签名数据
                            openAuthDTO.setSignData(signDataForAuth);
                            openAuthDTO.setRequestSign(getSignFromRequest(exchange.getRequest()));
                            openAuthDTO.setContentType( exchange.getRequest().getHeaders().getContentType() != null
                                    ? exchange.getRequest().getHeaders().getContentType().toString()
                                    : null);

                            //  调用 JWT 鉴权接口
                            jsonObject = openAuthApi.openJwtToApi(openAuthDTO);

                            if ("00000".equals(jsonObject.getString("code"))) {
                                if( exchange.getRequest().getMethod() == HttpMethod.POST ){
                                    ServerHttpRequest request = exchange.getRequest();
                                    if (signData instanceof CachedBody) {
                                        // 处理JSON请求体
                                        CachedBody cachedBody = (CachedBody) signData;
                                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                                            @Override
                                            public Flux<DataBuffer> getBody() {
                                                return cachedBody.getCachedFlux();
                                            }
                                        };
                                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                                    } else if (signData instanceof MultipartCachedBody) {
                                        // 处理Multipart请求体
                                        MultipartCachedBody multipartCachedBody = (MultipartCachedBody) signData;
                                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(request) {
                                            @Override
                                            public Flux<DataBuffer> getBody() {
                                                return multipartCachedBody.getCachedFlux();
                                            }
                                        };
                                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                                    }
                                }
                                return chain.filter(exchange);
                            } else {
                                ServerHttpResponse response = exchange.getResponse();
                                byte[] bits = JsonUtils.toJSONString(jsonObject).getBytes(StandardCharsets.UTF_8);
                                DataBuffer buffer = response.bufferFactory().wrap(bits);
                                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                                return response.writeWith(Mono.just(buffer));
                            }
                        } catch (Exception e) {
                            log.error("系统异常", e);
                            return Mono.error(new RuntimeException("签名验证过程异常：",e));
                        }
                    });
        }else{
            JSONObject jsonObject = openAuthApi.openAuthToApi(openAuthDTO);
            return handleResponse(exchange, chain, jsonObject);
        }
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
    private String getSignFromRequest(ServerHttpRequest request) {
        // 从URL参数获取sign
        List<String> signParams = request.getQueryParams().get("sign");
        return signParams != null && !signParams.isEmpty() ? signParams.get(0) : null;
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
        openAuthDTO.setToken(this.getAccessToken(exchange));
        openAuthDTO.setAppid(this.getAppId(exchange));
        openAuthDTO.setTimestamp(this.getTimestamp(exchange));
        openAuthDTO.setSign(this.getSign(exchange));
        openAuthDTO.setReferer(this.getReferer(exchange));
        openAuthDTO.setUrl(requestPath);
        if(paramMap!=null){
            //只取前200位进行验签
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
                    return appid;
                }
            }
            return appid;
        }
        return appid;
    }

    /**
     * 拿到access_token
     *
     * @param exchange ServerWebExchange
     * @return Result
     */
    private String getAccessToken(ServerWebExchange exchange) {
        String authorization = "";

        ServerHttpRequest request = exchange.getRequest();
        if (null != request.getHeaders().get("Upgrade")
                && "websocket".equalsIgnoreCase(request.getHeaders().get("Upgrade").get(0))) {
            if (null != request.getQueryParams().get("token")) {
                authorization = request.getQueryParams().get("token").get(0);
            }
        } else {
            try {
                    authorization = exchange.getRequest().getHeaders().get("authorization").get(0);
            } catch (NullPointerException exception) {
                if (HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
                    try {
                        authorization = exchange.getRequest().getQueryParams().get("token").get(0);
                    } catch (NullPointerException e) {
                        return authorization;
                    }
                }
            }
        }
        return authorization;
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
