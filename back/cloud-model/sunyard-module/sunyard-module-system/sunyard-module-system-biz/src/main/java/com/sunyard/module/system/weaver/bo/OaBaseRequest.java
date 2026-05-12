package com.sunyard.module.system.weaver.bo;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @Desc 泛微oa 组织架构同步通用请求对象
 */
@Slf4j
@Data
public abstract class OaBaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 基础的url信息
     */
    private String baseUrl;

    public abstract String getUrl();

    private static final String PARAMETER_PREFIX = "get";

    public String toPostRequest() {
        JSONObject jsonObject = new JSONObject();

        Method[] methods = getClass().getMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Method method : methods) {
            if (method.getName().startsWith(PARAMETER_PREFIX) && !method.getName().endsWith("Url")
                    && !method.getName().equals("getHttpEntity")
                    && !method.getName().equals("getClass")) {
                String fieldName = method.getName().substring(3);
                fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                try {
                    Object value = method.invoke(this);
                    if (value != null) {
                        jsonObject.put(fieldName, value);
                    }
                } catch (Exception e) {
                    log.error("获取get方法数据错误", e);
                    throw new RuntimeException("获取get方法数据错误", e);
                }
            }
        }
        JSONObject params = new JSONObject();
        params.put("params", jsonObject);
        return params.toString();
    }

    public HttpEntity<String> getHttpEntity(HttpHeaders headers) {
        return new HttpEntity<String>(toPostRequest(), headers);
    }

}
