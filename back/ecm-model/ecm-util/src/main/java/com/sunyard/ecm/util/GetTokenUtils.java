package com.sunyard.ecm.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @title 获取token
 * @description
 */
public class GetTokenUtils {


    /**
     * accessToken获取地址，目前写死可改到nacos上
     */
    private static String accessTokenUrl = "/web-api/auth/getOutApiToken";

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

    /**
     * token过期时间
     */
    private Date tokenExpires;

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
    public static String getAccessTokenByResponse(String ip,String accessTokenUrl,String appId, String appSecret) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ip + accessTokenUrl + "?");
        stringBuilder.append("appId=" + appId + "&");
        stringBuilder.append("appSecret=" + appSecret);
        JSONObject jsonObject = null;
        CloseableHttpClient httpClient = null;
        String accessToken="";
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
            /*if (split.length > 1) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    //到期时间
                    tokenExpires = sdf.parse(split[1]);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }*/
        } else {
            throw new RuntimeException(jsonObject.get("msg").toString());
        }
        return accessToken;
    }

    /**
     * 获取响应转成json格式。str
     *
     * @param response 响应头
     * @return Result
     * @throws IOException 异常
     */
    private static String getResponseString(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        StringBuilder requestBody = new StringBuilder();
        InputStream inputStream = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        return requestBody.toString();
    }

}
