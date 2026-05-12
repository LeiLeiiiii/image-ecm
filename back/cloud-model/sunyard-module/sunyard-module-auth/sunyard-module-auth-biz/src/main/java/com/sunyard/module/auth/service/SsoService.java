package com.sunyard.module.auth.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.http.param.MediaType;
import com.sunyard.framework.common.exception.SunyardException;
import com.sunyard.framework.common.util.AssertUtils;
import com.sunyard.framework.common.util.PasswordUtils;
import com.sunyard.framework.common.util.RsaUtils;
import com.sunyard.module.auth.util.CommonHttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
public class SsoService {


    @Value("${sso.chinaLinkUrl:https://chinalink-test.dcnbiz.com/}")
    private String chinaLinkUrl;
    @Value("${sso.clientId:3342bd869d4c4a53}")
    private String clientId;
    @Value("${sso.clientSecret:049da39cfb094189b626b64da9294189}")
    private String clientSecret;
    @Value("${sso.appId:}")
    private String appId;
    @Value("${sso.appSecret:}")
    private String appSecret;
    @Value("${sso.redirectUri:http://127.0.0.1:28081/account/sso/callback}")
    private String redirectUri;
    @Value("${sso.rsa.private-key:}")
    private String rsaPrivateKey;


    /**
     * 封装sso请求code地址
     */
    public String getCallBackUrl() {
        String redirectUrl = "";
        try {
            // 构造重定向URL
            redirectUrl = chinaLinkUrl + "esc-sso/oauth2.0/authorize"+
                    "?client_id=" + clientId +
                    "&response_type=code" +
                    "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");
        }catch (Exception e){
            log.error("构造重定向URL失败",e);
        }
        return redirectUrl;
    }

    /**
     * 获取sso登录用户
     */
    public String handleSsoCallback(String code, HttpServletRequest request) throws Exception {
        // 1. 使用code换取access_token
        String accessToken = getAccessToken(code);
        AssertUtils.isNull(accessToken, "获取access_token失败");

        // 2. 使用access_token获取用户信息
        String userInfo = getUserInfo(accessToken);
        AssertUtils.isNull(userInfo, "获取用户账号失败");

        return userInfo;
    }

    private String getAccessToken(String code) throws Exception {
        //请求参数拼到url后面
        StringBuilder tokenUrl = new StringBuilder(chinaLinkUrl + "esc-sso/oauth2.0/accessToken");
        tokenUrl.append("?grant_type=authorization_code");
        tokenUrl.append("&client_id=").append(clientId);
        tokenUrl.append("&client_secret=").append(clientSecret);
        tokenUrl.append("&code=").append(code);
        tokenUrl.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
        tokenUrl.append("&oauth_timestamp=").append(System.currentTimeMillis());

        JSONObject jsonObject = new JSONObject();

        log.info("获取accessToken地址：{}，请求参数：{}", tokenUrl, jsonObject);
        String response = executePostRequest(tokenUrl.toString(),jsonObject);
        log.info("response : {}",response);
        JSONObject result = JSONObject.parseObject(response);
        log.info("result : {}",result);
        return result.get("access_token").toString();
    }

    private String getUserInfo(String accessToken) throws Exception {
        String accountNo = "";
        String profileUrl = chinaLinkUrl + "/esc-sso/oauth2.0/profile?access_token=" + accessToken;
        log.info("获取UserToken地址：{}，请求参数：{}",profileUrl,accessToken);
        String response = executeGetRequest(profileUrl);
        log.info("getUserInfo -> response: {}",response);
        JSONObject result = JSONObject.parseObject(response);
        log.info("获取userToken结果：{}",result);
        if (result != null){
            Map<String,String> attributesList = (Map<String,String>)result.get("attributes");
            accountNo = attributesList.get("account_no");
        }
        return accountNo;
    }

    private String executePostRequest(String url, JSONObject requestBody) throws Exception {
        // 1. 构建忽略 SSL 证书校验的 HttpClient
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (x509Certificates, s) -> true) // 信任所有证书
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE) // 忽略主机名验证
                .build();

        // 2. 创建并配置 POST 请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8));

        // 3. 执行请求并获取响应
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return getResponseString(response);
        }
    }


    private String executePostRequest1(String url, JSONObject requestBody) throws Exception {
        CloseableHttpClient httpClient = CommonHttpClientUtils.createHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return getResponseString(response);
        }
    }

    private String executeGetRequest(String url) throws Exception {
        // 构建忽略 SSL 证书校验的 HttpClient
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (x509Certificates, s) -> true) // 信任所有证书
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE) // 忽略主机名验证
                .build();

        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return getResponseString(response);
        }
    }

    private String getResponseString(CloseableHttpResponse response) throws IOException {
        if (response == null || response.getEntity() == null) {
            return null;
        }
        return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
    }

    public String getRsaPassWord( String password) {
        try {
            //RSA解密密码
            password = RsaUtils.decrypt(password, rsaPrivateKey);
        }catch (Exception e){
            throw new SunyardException("密码解密失败");
        }
        return password;
    }

    public boolean checkApp(String appId, String appSecret) {
        return appId.equals(this.appId) && appSecret.equals(this.appSecret);
    }
}
