package com.sunyard.module.auth.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import com.sunyard.module.auth.constant.CachePrefixConstants;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.redis.util.RedisUtils;
import com.sunyard.module.auth.config.properties.AuthOauth2Properties;
import com.sunyard.module.auth.oauth.response.ResponseCodeEnum;
import com.sunyard.module.auth.oauth.response.Result;
import com.sunyard.module.system.api.ApiAuthApi;

import lombok.extern.slf4j.Slf4j;


/**
 * @author P-JWei
 * @date 2023/8/25 9:28:33
 * @title
 * @description
 */
@Slf4j
@Service
public class OpenAuthService {

    private static final String HOST = "http://localhost:";
    private static final String ACCESS_TOKEN_URL = "/oauth/token";
    @Resource
    private RedisUtils redisUtil;
    @Resource
    private ApiAuthApi apiAuthApi;
    @Resource
    private AuthOauth2Properties authOauth2Properties;

    /**
     * 获取token
     * @param appId appId
     * @param appSecret appSecret
     * @return Result
     */
    public Result getAccessToken(String appId, String appSecret) {
        //1、判断appId是否开启授权，未授权直接返回空
        if (!apiAuthApi.getIsAuthByAppId(appId).getData()) {
            return Result.error(ResponseCodeEnum.FAIL_UNAUTH.getMsg(), ResponseCodeEnum.FAIL_UNAUTH.getCode());
        }
        //2、判断appId是否有权限接口，无权限直接返回空
        if (apiAuthApi.getApiCodeByAppId(appId).getData().isEmpty()) {
            return Result.error(ResponseCodeEnum.FAIL_NOAPI.getMsg(), ResponseCodeEnum.FAIL_NOAPI.getCode());
        }
        //3、先从redis拿accessToken
        //有即返回，无走下面
        String accessToken = redisUtil.get(CachePrefixConstants.AUTH + appId);
        if (StringUtils.hasText(accessToken)) {
            return Result.success(accessToken);
        }
        String url = splicingParams(HOST + authOauth2Properties.getPort() + ACCESS_TOKEN_URL, appId, appSecret, "password", null);
        return checkToken(appId, toToken(new HttpPost(url)));
    }

    /**
     * 获取私钥
     * */
    public Result getPrivateKey(String publicKey) {
        return Result.success(apiAuthApi.getSecretKeyByPublicKey(publicKey.replaceAll(" ","+")).getData());
    }

    /**
     * 刷新apiToken
     * */
    public Result getRefreshApiToken(String appId, String appSecret,String refreshToken) {
        //1、判断appId是否开启授权，未授权直接返回空
        if (!apiAuthApi.getIsAuthByAppId(appId).getData()) {
            return Result.error(ResponseCodeEnum.FAIL_UNAUTH.getMsg(), ResponseCodeEnum.FAIL_UNAUTH.getCode());
        }
        //2、判断appId是否有权限接口，无权限直接返回空
        if (apiAuthApi.getApiCodeByAppId(appId).getData().isEmpty()) {
            return Result.error(ResponseCodeEnum.FAIL_NOAPI.getMsg(), ResponseCodeEnum.FAIL_NOAPI.getCode());
        }
        //3、先从redis拿accessToken
        //有即返回，无走下面
//        String accessToken = redisUtil.get(appId);
//        if (StringUtils.hasText(accessToken)) {
//            return Result.success(accessToken);
//        }
        String url = splicingParams(HOST + authOauth2Properties.getPort() + ACCESS_TOKEN_URL, appId, appSecret, "refresh_token", refreshToken);
        return checkToken(appId, toToken(new HttpPost(url)));
    }

    /**
     * 获取accessToken存入redis并且返回
     *
     * @param appId      appId
     * @param jsonObject jsonObject
     * @return Result
     */
    private Result checkToken(String appId, JSONObject jsonObject) {
        //正常返回size>2
        int size = jsonObject.size();
        if (size > 2) {
            //accessToken
            String accessToken = jsonObject.get("access_token").toString();
            //过期时间
            Integer expiresIn = (Integer) jsonObject.get("expires_in");
            String refreshToken = (String) jsonObject.get("refresh_token");
            //计算过期时间并且返回
            Calendar calendar = Calendar.getInstance();
            // 将秒数添加到当前时间
            calendar.add(Calendar.SECOND, expiresIn);
            Date newTime = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String tokenAndTime = accessToken + "||" + sdf.format(newTime)+"||"+refreshToken+"||"+authOauth2Properties.getApiTokenTimeOut();
            //过期时间存入redis
            redisUtil.set(CachePrefixConstants.AUTH + appId, tokenAndTime, Long.valueOf(expiresIn), TimeUnit.SECONDS);
            return Result.success(tokenAndTime);
        }
        //不正常返回失败原因即可
        return Result.error(jsonObject.get("error_description").toString(), ResponseCodeEnum.FAIL_APPID);
    }

    /**
     * 拼接url
     *
     * @param url          请求url
     * @param appId        appId
     * @param appSecret    appSecret
     * @param grantType    请求类别
     * @param refreshToken 刷新token
     * @return Result
     */
    private String splicingParams(String url, String appId, String appSecret, String grantType, String refreshToken) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url).append("?");
        stringBuilder.append("username=").append(appId).append("&");
        stringBuilder.append("password=").append(appSecret).append("&");
        stringBuilder.append("client_id=").append(authOauth2Properties.getClientId()).append("&");
        stringBuilder.append("client_secret=").append(authOauth2Properties.getClientSecret()).append("&");
        stringBuilder.append("grant_type=").append(grantType);
        if (null != refreshToken) {
            stringBuilder.append("&refresh_token=").append(refreshToken);
        }
        return stringBuilder.toString();
    }

    /**
     * 获取token
     *
     * @param httpPost post请求
     * @return Result
     */
    private synchronized JSONObject toToken(HttpPost httpPost) {
        CloseableHttpClient build = HttpClientBuilder.create().build();
        JSONObject jsonObject = null;
        String responseString;
        boolean isFail = true;
        int count = 10;
        try {
            while (isFail && count > 0) {
                --count;
                CloseableHttpResponse execute = build.execute(httpPost);
                responseString = getResponseString(execute);
                jsonObject = JSONObject.parseObject(responseString);
                if (jsonObject.size() > 2) {
                    isFail = false;
                }
            }
        } catch (Exception e) {
            log.error("系统异常", e);
            jsonObject.put("error", "unknown exception");
            jsonObject.put("error_description", "Failure due to unknown reasons");
        }
        return jsonObject;
    }

    /**
     * 获取响应str
     *
     * @param response 响应头
     * @return Result
     * @throws IOException io异常
     */
    private String getResponseString(CloseableHttpResponse response) throws IOException {
        try (InputStream inputStream = response.getEntity().getContent();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            return requestBody.toString();
        } finally {
            if (response != null) {
                response.close(); // 手动关闭 response
            }
        }
    }

}
