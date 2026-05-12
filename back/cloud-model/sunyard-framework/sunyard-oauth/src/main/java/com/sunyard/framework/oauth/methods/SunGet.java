package com.sunyard.framework.oauth.methods;

import org.apache.http.client.methods.HttpRequestBase;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Instant;

/**
 * @author P-JWei
 * @date 2023/8/28 9:39:36
 * @title
 * @description
 */
public class SunGet extends HttpRequestBase {

    public final static String METHOD_NAME = "GET";

    private String url;

    private Object objBody;

    private String privateKey;

    public SunGet() {
        super();
    }

    public SunGet(final URI uri) {
        super();
        setURI(uri);
    }

    public SunGet(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    private SunGet(String url, Object objBody) {
        this.url = url;
        this.objBody = objBody;
        this.setURI(URI.create(url));
        this.setHeader();
    }

    private void setHeader() {
        this.setHeader("Authorization", "");
        //加签
        this.setHeader("sign", "");
        //加时间戳
        this.setHeader("timestamp", Instant.now().toEpochMilli() + "");
        //加入加密条件
        this.setHeader("privateKey", privateKey);
        //拼接参数
        this.setURI(URI.create(splitParam()));
    }

    private String splitParam(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url + "?");
        Class<?> aClass = objBody.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                stringBuilder.append(field.getName() + "=" + aClass.getMethod(field.getName()).invoke(objBody) + "&");
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
