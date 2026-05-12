package com.sunyard.framework.oauth.methods;

import com.alibaba.fastjson.JSON;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author P-JWei
 * @date 2023/7/18 14:52
 * @title post请求
 * @description
 */
public class SunPost extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "POST";

    private String contentType = "application/json";

    private String url;

    private Object objBody;

//    private String publicKey;

    private String privateKey;

    private String referer;

    /**
     * publicKey获取地址，目前写死可改到nacos上
     */
    private static String accessTokenUrl = "/web-api/auth/getPrivateKey";

    /**
     * 创建对象时不指定ip，默认本地
     */
    private String ip = "http://localhost:8080";

    public SunPost(String url, String privateKey, Object objBody, String referer,String ip) {
        this.setURI(URI.create(url));
        this.url = url;
        this.objBody = objBody;
        this.privateKey = privateKey;
        this.referer = referer;
        this.ip = ip;
        this.setHeader();
    }

    public SunPost(String contentType, String url,  String privateKey, Object objBody, String referer,String ip) {
        this.setURI(URI.create(url));
        this.contentType = contentType;
        this.url = url;
        this.objBody = objBody;
        this.privateKey = privateKey;
        this.referer = referer;
        this.ip = ip;
        this.setHeader();
    }



    private void setHeader() {
        this.setHeader("Authorization", "");
        //加签
        this.setHeader("sign", "");
        //加入referer
        this.setHeader("Referer", referer);
        //加时间戳
        this.setHeader("timestamp", Instant.now().toEpochMilli() + "");
        //加入加密条件
//        this.setHeader("publicKey", publicKey);
        this.setHeader("privateKey", privateKey);
        //添加判断走params还是body
        if (null != contentType && !"".equals(contentType)) {
            //如果是multipart/form-data，让参数全走params,body只留文件流
            if (ContentType.MULTIPART_FORM_DATA.toString().contains(contentType)) {
                //拼接params并且拼接文件参数
                this.setURI(URI.create(splitParam()));
            } else if (ContentType.APPLICATION_JSON.toString().contains(contentType)) {
                this.setHeader("Content-Type", contentType);
                String jsonBody = JSON.toJSONString(objBody);
                ByteArrayEntity entity = new ByteArrayEntity(jsonBody.getBytes(StandardCharsets.UTF_8));
                entity.setContentType(ContentType.APPLICATION_JSON.toString());
                this.setEntity(entity);
            } else if (ContentType.APPLICATION_FORM_URLENCODED.toString().contains(contentType)) {
                this.setHeader("Content-Type", contentType);
                try {
                    this.setEntity(new UrlEncodedFormEntity(objectToListMap(objBody), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

    private String splitParam() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url + "?");
        Class<?> aClass = objBody.getClass();
        Field[] fields = aClass.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object o = field.get(objBody);
                if (o instanceof MultipartFile) {
                    MultipartFile multipartFile = (MultipartFile) o;
                    //拼接文件
                    // 创建MultipartEntityBuilder并添加文件部分
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    //加上此行代码解决返回中文乱码问题
                    builder.setCharset(Charset.forName("utf-8"));
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    //Spring的MultipartFile实现已经正确处理了资源管理不需要进行try或者手动流释放
                    builder.addBinaryBody("file", multipartFile.getInputStream(), ContentType.MULTIPART_FORM_DATA, multipartFile.getOriginalFilename());
                    this.setEntity(builder.build());
                } else if (o instanceof MultipartFile[]) {
                    // 处理MultipartFile数组
                    MultipartFile[] multipartFiles = (MultipartFile[]) o;
                    //拼接文件
                    // 创建MultipartEntityBuilder并添加文件部分
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    //加上此行代码解决返回中文乱码问题
                    builder.setCharset(Charset.forName("utf-8"));
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    for (MultipartFile multipartFile : multipartFiles) {
                        builder.addBinaryBody("file", multipartFile.getInputStream(), ContentType.MULTIPART_FORM_DATA, multipartFile.getOriginalFilename());
                    }
                    this.setEntity(builder.build());
                } else {
                    Object invoke = getGetMethod(objBody.getClass(), field.getName()).invoke(objBody);
                    String jsonString = invoke.toString();
                    String encodedJson = URLEncoder.encode(jsonString, StandardCharsets.UTF_8.name());
                    stringBuilder.append(field.getName() + "=" + encodedJson + "&");
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("参数反射获取失败", e);
            } catch (IOException e) {
                throw new RuntimeException("文件流处理失败", e);
            }
        }

        // 优化最后一个字符的处理
        if (stringBuilder.length() > 0 && stringBuilder.charAt(stringBuilder.length() - 1) == '&') {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    private Method getGetMethod(Class objectClass, String fieldName) {
        StringBuffer sb = new StringBuffer();
        sb.append("get");
        sb.append(fieldName.substring(0, 1).toUpperCase());
        sb.append(fieldName.substring(1));
        try {
            return objectClass.getMethod(sb.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<NameValuePair> objectToListMap(Object objBody) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (objBody instanceof Map) {
            Map<String, Object> objMap = (HashMap<String, Object>) objBody;
            objMap.forEach((key, value) -> params.add(new BasicNameValuePair(key, value + "")));
        } else {
            Class<?> aClass = objBody.getClass();
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                try {
                    params.add(new BasicNameValuePair(field.getName(), getGetMethod(objBody.getClass(), field.getName()).invoke(objBody) + ""));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return params;
    }

    @Override
    public String getMethod() {
        return "POST";
    }
}
