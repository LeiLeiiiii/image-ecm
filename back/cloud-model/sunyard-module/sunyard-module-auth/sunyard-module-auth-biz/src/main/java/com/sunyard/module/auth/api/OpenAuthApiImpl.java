package com.sunyard.module.auth.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.shiro.util.AntPathMatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.sunyard.framework.common.result.Result;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.auth.api.dto.OpenAuthDTO;
import com.sunyard.module.auth.constant.CachePrefixConstants;
import com.sunyard.module.auth.oauth.config.ResourceServerConfig;
import com.sunyard.module.auth.oauth.dto.CustomUser;
import com.sunyard.module.auth.oauth.response.ResponseCodeEnum;
import com.sunyard.module.auth.util.JwtUtil;
import com.sunyard.module.auth.util.OAuthHandleUtils;
import com.sunyard.module.system.api.SysAuthApi;
import com.sunyard.module.system.api.dto.SysApiSystemDTO;

/**
 * @author P-JWei
 * @date 2023/11/6 13:50:49
 * @title
 * @description
 */
@RestController
public class OpenAuthApiImpl implements OpenAuthApi {

    public static final String JWT_TOKEN = "jwtToken:";
    public static final String JWT_TOKEN_CACHE = "jwtTokenCache:";
    @Value("${gateway.sign-verify.SIGN_SECRET:sunyard}")
    private String SIGN_SECRET;
    @Resource
    @Lazy
    private TokenStore tokenStore;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private ResourceServerTokenServices tokenServices;
    @Resource
    private OAuthHandleUtils authHandleUtils;
    @Resource
    private SysAuthApi sysAuthApi;

    @Override
    public Result<Boolean> revokeToken(String appId) {
        //从redis获取现存token
        String token = redisUtils.get(CachePrefixConstants.AUTH + appId);
        //删除redis里的数据
        redisUtils.del(CachePrefixConstants.AUTH + appId);
        if (StringUtils.hasText(token)) {
            //使token失效
            OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
            if (oAuth2AccessToken != null) {
                tokenStore.removeAccessToken(oAuth2AccessToken);
            }
        }
        return Result.success(true);
    }

    @Override
    public JSONObject openAuthToApi(OpenAuthDTO openAuthDTO) {
        return openAuthToApi(openAuthDTO.getToken(), openAuthDTO.getAppid(),
                openAuthDTO.getTimestamp(), openAuthDTO.getSign(), openAuthDTO.getReferer(),
                openAuthDTO.getUrl(), openAuthDTO.getParam());
    }

    @Override
    public Result<String> getTokenJwt(String flagId) {
        String s = JwtUtil.generateToken(flagId);
        renewToken(s, flagId);
        return Result.success(JWT_TOKEN + s);
    }

    /**
     * token续费
     * @param token
     * @return
     */
    public void renewToken(String token, String appIdUsername) {
        String key = JWT_TOKEN + token;
        setjwtTokenInRedis(appIdUsername, key);
    }

    @Override
    public JSONObject openJwtToApi(OpenAuthDTO openAuthDTO) {
        String token = openAuthDTO.getToken();
        String appidAndUsername = openAuthDTO.getAppid();
        String url = openAuthDTO.getUrl();

        String key = CachePrefixConstants.AUTH + JWT_TOKEN + token;
        Boolean b = redisUtils.hasKey(key);
        if (b) {

            //  签名校验（核心：仅对比签名，不处理请求体解析）
            boolean skipSignVerify = false; // 标记是否跳过签名校验
            if (!authHandleUtils.signVerifyFilter(url)) { // 需要签名校验的 URL
                Object signData = openAuthDTO.getSignData();
                String requestSign = openAuthDTO.getRequestSign();
                if (requestSign == null || requestSign.isEmpty()) {
                    String contentType = openAuthDTO.getContentType();
                    // 未传签名且有contentType说明有请求数据，参数错误
                    if (contentType != null && !contentType.isEmpty()) { // 适配 String 判断
                        return JSONObject.parseObject(
                                JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result
                                        .error(ResponseCodeEnum.FAIL_SIGN)));
                    } else {
                        skipSignVerify = true;
                    }
                }
                //  待签名数据为空 → 失败（兜底）
                if (!skipSignVerify && signData != null && !"success".equals(signData)) {
                    // 生成待签名字符串并对比
                    String dataToSign = buildSignData(signData) + "&" + SIGN_SECRET;
                    String computedSign = generateSign(dataToSign);
                    // 签名不匹配 → 失败
                    if (!computedSign.equals(requestSign)) {
                        return JSONObject.parseObject(
                                JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result
                                        .error(ResponseCodeEnum.FAIL_SIGN)));
                    }
                }
            }
            //从缓存拿结果，看是否存在
            String keyCache = CachePrefixConstants.AUTH + JWT_TOKEN_CACHE + token;
            if (redisUtils.hasKey(keyCache)) {
                return JSONObject.parseObject(JSON
                        .toJSONString(com.sunyard.module.auth.oauth.response.Result.success("")));
            }
            //10s内不重复校验
            redisUtils.set(keyCache, "true", 10, TimeUnit.SECONDS);

            //通过token对象
            if (!JwtUtil.validateToken(token)) {
                return JSONObject
                        .parseObject(JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result
                                .error(ResponseCodeEnum.FAIL_TOKEN)));
            }
            //续费
            renewToken(token, appidAndUsername);
            return JSONObject.parseObject(
                    JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result.success("")));
        } else {
            return JSONObject
                    .parseObject(JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result
                            .error(ResponseCodeEnum.FAIL_TOKEN)));

        }
    }

    private void setjwtTokenInRedis(String appidAndUsername, String key) {
        Map map = new HashMap();
        map.put("appIdUsername", appidAndUsername);
        redisUtils.set(CachePrefixConstants.AUTH + key, JSONObject.toJSONString(map), 30, TimeUnit.MINUTES);
    }

    //     构建待签名字符串
    private String buildSignData(Object signData) {
        StringBuilder sb = new StringBuilder();

        if (signData instanceof Map) {
            // 处理GET参数、Form参数、JSON转来的Map（关键修改：Map<?, ?> 兼容 Object 值）
            ((Map<?, ?>) signData).forEach((key, value) -> {
                // 关键修改：value 用 String.valueOf() 统一转字符串（避免 LinkedHashMap 转 String 报错）
                String keyStr = String.valueOf(key);
                String valueStr = convertValueToString(value);
                sb.append(keyStr).append("=").append(valueStr).append("&");
            });
            // 移除最后一个&符号
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
        } else if (signData instanceof JsonNode) {
            // 处理JSON参数
            sb.append(signData.toString());
        }

        return sb.toString();
    }

    /**
     * 递归转换值为字符串，统一保留null字符
     */
    private String convertValueToString(Object value) {
        if (value == null) {
            return "null"; // null直接返回"null"字符串
        }
        // 处理集合/数组类型
        if (value instanceof Iterable || value.getClass().isArray()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            // 遍历集合/数组元素
            Iterable<?> iterable = value instanceof Iterable ? (Iterable<?>) value
                    : Arrays.asList((Object[]) value);
            for (Object elem : iterable) {
                sb.append(convertValueToString(elem)).append(",");
            }
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 1);
            }
            sb.append("]");
            return sb.toString();
        }
        // 处理嵌套Map类型
        if (value instanceof Map) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            ((Map<?, ?>) value).forEach((k, v) -> {
                sb.append(String.valueOf(k)).append("=").append(convertValueToString(v))
                        .append(",");
            });
            if (sb.length() > 1) {
                sb.setLength(sb.length() - 1);
            }
            sb.append("}");
            return sb.toString();
        }
        // 基础类型直接转字符串
        return String.valueOf(value);
    }

    // 生成MD5签名
    private String generateSign(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }

    /**
     * api认证
     *
     * @param token     token
     * @param appid     apiId
     * @param timestamp 时间戳
     * @param sign      加签
     * @param referer   referer
     * @param url       url
     * @param param     参数
     * @return Result
     */
    public JSONObject openAuthToApi(String token, String appid, String timestamp, String sign,
                                    String referer, String url, String param) {
        //先从缓存拿结果，看是否存在
        String redisPassResult = authHandleUtils.getRedisPassResult("cache_" + appid + url);
        if (StringUtils.hasText(redisPassResult)) {
            return JSONObject.parseObject(redisPassResult);
        }
        //通过token对象
        OAuth2Authentication oAuth2Authentication = tokenServices.loadAuthentication(token);
        //拿到权限集
        Collection<GrantedAuthority> authorities = oAuth2Authentication.getAuthorities();
        //拿到认证对象
        CustomUser customUser = (CustomUser) oAuth2Authentication.getPrincipal();
        // 使用Stream将权限集合转换为字符串集合
        Collection<String> authorityStrings = authorities.stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        String username = customUser.getUsername();
        Result<SysApiSystemDTO> systemDTOResult = sysAuthApi.getApiByAppId(username);
        SysApiSystemDTO sysApiSystem = systemDTOResult.getData();
        if (!isPass(authorityStrings, url)) {
            return JSONObject
                    .parseObject(JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result
                            .error(ResponseCodeEnum.FAIL_TOKEN)));
        }
        if (!url.contains("/api/ws/triggerQuery")) {
            if (!authHandleUtils.timestampFilter(timestamp, url)) {
                return JSONObject
                        .parseObject(JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result
                                .error(ResponseCodeEnum.FAIL_TIMESTAMP)));
            }
            if (!authHandleUtils.refererFilter(customUser.getReferer(), referer, url)) {
                return JSONObject
                        .parseObject(JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result
                                .error(ResponseCodeEnum.FAIL_REFERER)));
            }
            if (!authHandleUtils.verifySignatureFilter(appid, sign, timestamp, param, sysApiSystem,
                    url)) {
                return JSONObject
                        .parseObject(JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result
                                .error(ResponseCodeEnum.FAIL_SIGN)));
            }
        }

        JSONObject jsonObject = JSONObject.parseObject(
                JSON.toJSONString(com.sunyard.module.auth.oauth.response.Result.success("")));
        //验证通过后，缓存进redis，保证一定时间内无须重复验证
        authHandleUtils.toRedisPassResult("cache_" + appid + url, jsonObject.toJSONString(), 10L);
        //修改成功返回
        return jsonObject;
    }

    /**
     * 验证url权限
     *
     * @param authorities 权限集
     * @param url         接口url
     * @return Result
     */
    public boolean isPass(Collection<String> authorities, String url) {
        //获取所有需要校验url的key-value
        Map<String, String> apiMap = ResourceServerConfig.allApi;
        for (Map.Entry<String, String> entry : apiMap.entrySet()) {
            String pattern = entry.getKey();
            String value = entry.getValue();
            // 使用 路径匹配工具进行 URL 匹配
            if (pathMatches(value, url)) {
                //匹配成功，再判断所需的权限，当前appId权限是否拥有
                return authorities.contains(pattern);
            }
        }
        // 如果没有找到匹配的 URL，则返回 null 或者其他默认值
        return false;
    }

    /**
     * url匹配工具
     *
     * @param pattern 匹配格式
     * @param path    接口地址
     * @return Result
     */
    private boolean pathMatches(String pattern, String path) {
        // 使用 Shiro 自带的路径匹配工具进行 URL 匹配
        return new AntPathMatcher().matches(pattern, path);
    }

}
