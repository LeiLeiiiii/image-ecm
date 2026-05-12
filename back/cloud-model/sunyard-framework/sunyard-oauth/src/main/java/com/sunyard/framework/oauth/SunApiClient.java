package com.sunyard.framework.oauth;

import com.alibaba.fastjson.JSONObject;
import com.sunyard.framework.common.constant.LoginEncryptionConstant;
import com.sunyard.framework.common.util.encryption.Sm2Util;
import com.sunyard.framework.oauth.methods.SunPost;
import com.sunyard.framework.oauth.utils.CommonHttpClientUtils;
import com.sunyard.framework.oauth.utils.HttpGetBodyUtils;
import com.sunyard.framework.oauth.utils.RsaUtils;
import com.sunyard.framework.oauth.utils.SignatureUtils;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.util.Base64Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author P-JWei
 * @date 2023/7/18 14:28
 * @title sunApi客户端
 * @description
 */
public class SunApiClient {


    /**
     * accessToken获取地址，目前写死可改到nacos上
     */
    private static String accessTokenUrl = "/web-api/auth/getOutApiToken";
    private static String refreshTokenUrl = "/web-api/auth/getRefreshApiToken";
    /**
     * 创建对象时不指定ip，默认本地
     */
    private String ip = "http://localhost:8080";
    /**
     * appId，联系本系统获取
     */
    private String appId;

    /**
     * appSecret，联系本系统获取
     */
    private String appSecret;

    /**
     * 认证token
     */
    private String accessToken = "";

    private String refreshToken = "";
    private Date refreshTokenTime;
    private String limitSecond;
    /**
     * token过期时间
     */
    private Date tokenExpires;

    /**
     * 不指定ip构造器
     */
    public SunApiClient(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
        getAccessTokenByResponse();
    }

    /**
     * 指定ip构造器
     */
    public SunApiClient(String appId, String appSecret, String baseIp) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.ip = baseIp;
        getAccessTokenByResponse();
    }

    /**
     * 获取accessToken
     * 供其他需token使用的业务场景
     *
     * @return Result
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * 请求accessToken
     * 因token具有时效性，实时获取，
     */
    private void getAccessTokenByResponse() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ip + accessTokenUrl + "?");
        stringBuilder.append("appId=" + appId + "&");
        stringBuilder.append("appSecret=" + appSecret);
        JSONObject jsonObject = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = CommonHttpClientUtils.createHttpClient();
            CloseableHttpResponse execute = httpClient.execute(new HttpPost(stringBuilder.toString()));
            String responseString = getResponseString(execute);
            jsonObject = JSONObject.parseObject(responseString);
        } catch (Exception e) {

            throw new RuntimeException("未知异常", e);
        }
        if ("00000".equals(jsonObject.get("code").toString())) {
            String data = jsonObject.get("data").toString();
            //拆分回参
            String[] split = data.split("\\|\\|");
            //token
            accessToken = split[0];
            if (split.length > 1) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    //到期时间
                    tokenExpires = sdf.parse(split[1]);
                    if (split.length > 2) {
                        refreshToken = split[2];
                    }
                    if(split.length>3){
                        limitSecond = split[3];
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new RuntimeException(jsonObject.get("msg").toString());
        }
    }


    private void getRefreshTokenByResponse() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ip + refreshTokenUrl + "?");
        stringBuilder.append("appId=" + appId + "&");
        stringBuilder.append("appSecret=" + appSecret+ "&");
        stringBuilder.append("refreshToken=" + refreshToken);
        JSONObject jsonObject = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = CommonHttpClientUtils.createHttpClient();
            CloseableHttpResponse execute = httpClient.execute(new HttpPost(stringBuilder.toString()));
            String responseString = getResponseString(execute);
            jsonObject = JSONObject.parseObject(responseString);
        } catch (Exception e) {

            throw new RuntimeException("未知异常", e);
        }
        if ("00000".equals(jsonObject.get("code").toString())) {
            String data = jsonObject.get("data").toString();
            //拆分回参
            String[] split = data.split("\\|\\|");
            //token
            accessToken = split[0];
            if (split.length > 1) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    //到期时间
                    refreshTokenTime = sdf.parse(split[1]);
                    if (split.length > 2) {
                        refreshToken = split[2];
                    }
                    if(split.length>3){
                        limitSecond = split[3];
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new RuntimeException(jsonObject.get("msg").toString());
        }
    }

    /**
     * 发送请求
     *
     * @param request 请求头
     * @return Result
     * @throws IOException 异常
     */
    public String executeResponseToString(HttpRequestBase request) throws IOException {
        //判断如果当前时间大于了过期时间，则说明token已过期，需要重新获取
        handleToken();
        request.setHeader("appid", appId);
        CloseableHttpResponse execute = null;
        String responseString = "";
        boolean isFail = true;
        int count = 10;
        while (isFail && count > 0) {
            --count;
            request.setHeader("Authorization", accessToken);
            execute = this.execute(request);
            responseString = getResponseString(execute);
            if (responseString.contains("invalid_token")) {
                getAccessTokenByResponse();
            } else {
                isFail = false;

            }
        }
        return responseString;
    }

    /**
     * 发送请求
     *
     * @param request 请求头
     * @return Result
     * @throws IOException 异常
     */
    public CloseableHttpResponse executeResponse(HttpRequestBase request) throws IOException {
        handleToken();
        request.setHeader("Authorization", accessToken);
        request.setHeader("appid", appId);
        return this.execute(request);
    }

    private void handleToken() {
        if(this.limitSecond==null){
            this.limitSecond = "7200000";
        }
        //判断如果当前时间大于了过期时间，则说明token已过期，需要重新获取
        Date date = new Date();
        if (date.after(tokenExpires)) {
            getAccessTokenByResponse();
        }else if((tokenExpires.getTime()-date.getTime())<Integer.parseInt(this.limitSecond)){
            //如果已经临近失效，直接刷新token，
            if(refreshTokenTime==null||date.after(refreshTokenTime)){
                try {
                    getRefreshTokenByResponse();
                }catch (Exception e){
                    
                    getAccessTokenByResponse();
                }
            }
        }
    }

    private CloseableHttpResponse execute(HttpRequestBase httpRequestBase) {
        CloseableHttpClient httpClient = null;
        try {
            signEncrypt(httpRequestBase);
            //先注释，data加密方法
//            dataEncrypt(httpRequestBase);
            httpRequestBase.setHeader("appid", appId);
            httpClient = CommonHttpClientUtils.createHttpClient();
            CloseableHttpResponse execute = httpClient.execute(httpRequestBase);
            return execute;
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * sign加密
     *
     * @param request 请求头
     */
    private void signEncrypt(HttpRequest request){
        String bodyData = "";
        if (request.getHeaders("Content-Type").length > 0) {
            if (ContentType.MULTIPART_FORM_DATA.toString().contains(request.getHeaders("Content-Type")[0].getValue())) {
                //拼接参数(只取前200位进行加密)
                bodyData = getRequestParam(request);
            } else {
                bodyData = getRequestBody(request);
            }
        }
        String signEncrypt = "";
        Integer loginEncryption = 1;
        switch (loginEncryption) {
            case LoginEncryptionConstant.RSA_LOGIN_TYPE:
                signEncrypt = SignatureUtils.sign(appId + bodyData, request.getHeaders("privateKey")[0].getValue());
                break;
            default:
                signEncrypt = Sm2Util.signWithSM2Standard(appId + bodyData, request.getHeaders("privateKey")[0].getValue());
        }
        request.setHeader("sign", signEncrypt);
        request.setHeader("bodyData", Base64Utils.encodeToString(bodyData.getBytes()));

    }

    /**
     * data加密
     *
     * @param request 请求头
     */
    private void dataEncrypt(HttpRequest request) {
        //true 则加密body
        if (null != request.getHeaders("publicKey")[0].getValue()) {
            String dataEncrypt = "";
            try {
                dataEncrypt = RsaUtils.encrypt(getRequestBody(request), request.getHeaders("publicKey")[0].getValue());
            } catch (Exception e) {
                //加密失败
                throw new RuntimeException(e);
            }
            //替换未加密的dataBody
            replaceBody(request, dataEncrypt);
        }
    }

    /**
     * 获取请求参数
     *
     * @param request 请求头
     * @return Result
     */
    private String getRequestParam(HttpRequest request) {
        RequestLine requestLine = request.getRequestLine();
        String uri = requestLine.getUri();
        String paramString = uri.split("\\?")[1];
        Map<String, String> map = new LinkedHashMap<>();
        if (paramString.length() > 0) {
            List<NameValuePair> queryParams = URLEncodedUtils.parse(paramString, StandardCharsets.UTF_8);
            for (int i = 0; i < queryParams.size(); i++) {
                map.put(queryParams.get(i).getName(), queryParams.get(i).getValue());
            }
            String s = JSONObject.toJSONString(map);
            return s.length() < 200 ? s : s.substring(0, 200);
        }
        return "";
    }

    /**
     * 拿到请求body数据
     *
     * @param request 请求头
     * @return Result
     * @throws IOException 异常
     */
    private String getRequestBody(HttpRequest request) {
        String bodyString = HttpGetBodyUtils.getBodyString((SunPost) request);
        return bodyString.length() < 200 ? bodyString : bodyString.substring(0, 200);
    }

    /**
     * 替换body
     *
     * @param request     请求头
     * @param dataEncrypt 加密后得data
     */
    private void replaceBody(HttpRequest request, String dataEncrypt) {
        if (request instanceof SunPost) {
            SunPost sunPostRequest = (SunPost) request;
            ByteArrayEntity entity = new ByteArrayEntity(dataEncrypt.getBytes(StandardCharsets.UTF_8));
            entity.setContentType("application/json");
            sunPostRequest.setEntity(entity);
        }
    }

    /**
     * 获取响应转成json格式。str
     *
     * @param response 响应头
     * @return Result
     * @throws IOException 异常
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
