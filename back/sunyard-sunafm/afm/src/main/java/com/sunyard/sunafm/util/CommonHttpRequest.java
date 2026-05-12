package com.sunyard.sunafm.util;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author P-JWei
 * @date 2024/3/25 14:42:30
 * @title
 * @description
 */
//todo 底座有，或者用spring的rest模板
@Slf4j
@Data
public class CommonHttpRequest {

    public static String METHOD_NAME;

    private String url;

    private List<Header> headers = new ArrayList<>();

    private HttpEntity entity;


    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public CommonHttpRequest(final String uri) {
        url = uri;
    }

    /**
     * post请求
     *
     * @param url
     * @return
     */
    public static CommonHttpRequest post(String url) {
        METHOD_NAME = "POST";
        return new CommonHttpRequest(url);
    }

    /**
     * get请求
     *
     * @param url
     * @return
     */
    public static CommonHttpRequest get(String url) {
        METHOD_NAME = "GET";
        return new CommonHttpRequest(url);
    }

    /**
     * url拼接
     *
     * @param obj
     * @return
     */
    public CommonHttpRequest params(Object obj) {
        if (null != obj) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(url + "?");
            Class<?> aClass = obj.getClass();
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    stringBuilder.append(field.getName() + "=" + getGetMethod(obj.getClass(), field.getName()).invoke(obj) + "&");
                } catch (Exception e) {
                    return null;
                }
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            this.url = stringBuilder.toString();
        }
        return this;
    }

    /**
     * 表单
     *
     * @return
     */
    public CommonHttpRequest form(Map<String, String> map) {
        if (!CollectionUtils.isEmpty(map)) {
            List<NameValuePair> formParams = new ArrayList<>();
            for (Map.Entry<String, String> item : map.entrySet()) {
                formParams.add(new BasicNameValuePair(item.getKey(), item.getValue()));
            }
            try {
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formParams, "UTF-8");
                this.setEntity(formEntity);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    /**
     * json
     *
     * @return
     */
    public CommonHttpRequest body(String body) {
        if (null != body && !body.trim().isEmpty() && isJson(body)) {
            headers.add(new BasicHeader("Content-Type", "application/json"));
            StringEntity stringEntity = new StringEntity(body, "UTF-8");
            this.setEntity(stringEntity);
        }
        return this;
    }

    /**
     * 文件流
     * @param data
     * @return
     */
    public CommonHttpRequest octetStream(byte[] data) {
        if (data.length > 0) {
            headers.add(new BasicHeader("Content-Type", "application/octet-stream"));
            InputStream inputStream = new ByteArrayInputStream(data);
            HttpEntity entity = new InputStreamEntity(inputStream, ContentType.APPLICATION_OCTET_STREAM);
            this.setEntity(entity);
        }
        return this;
    }

    /**
     * 设置自定义header参数
     *
     * @param map
     * @return
     */
    public CommonHttpRequest header(Map<String, String> map) {
        if (!CollectionUtils.isEmpty(map)) {
            for (Map.Entry<String, String> item : map.entrySet()) {
                headers.add(new BasicHeader(item.getKey(), item.getValue()));
            }
        }
        return this;
    }

    /**
     * 发送请求
     */
    public HttpEntity execute() {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = CommonHttpClientUtils.createHttpClient();
            if ("POST".equals(METHOD_NAME)) {
                HttpPost httpPost = new HttpPost(this.url);
                headers.forEach(httpPost::setHeader);
                httpPost.setEntity(entity);
                return httpClient.execute(httpPost).getEntity();
            } else if ("GET".equals(METHOD_NAME)) {
                HttpGet httpGet = new HttpGet(this.url);
                headers.forEach(httpGet::setHeader);
                return httpClient.execute(httpGet).getEntity();
            } else {
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 判断是否为json格式
     *
     * @param str
     * @return
     */
    private boolean isJson(String str) {
        try {
            JSONObject.parseObject(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Method getGetMethod(Class objectClass, String fieldName) {
        StringBuffer sb = new StringBuffer();
        sb.append("get");
        sb.append(fieldName.substring(0, 1).toUpperCase());
        sb.append(fieldName.substring(1));
        try {
            return objectClass.getMethod(sb.toString());
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 获取响应转成json格式。str
     *
     * @param entity
     * @return
     * @throws IOException
     */
    public static String getResponseString(HttpEntity entity) {
        StringBuilder requestBody = new StringBuilder();
        try {
            InputStream inputStream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line="";
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        } catch (Exception e) {

        }

        return requestBody.toString();
    }

//    public static void main(String[] args) throws IOException {
//        HttpEntity execute = CommonHttpRequest.post("http://127.0.0.1:8080/test")
//                .body("json参数")
//                .execute();
//        System.out.println(execute.getContent());
//    }
}
