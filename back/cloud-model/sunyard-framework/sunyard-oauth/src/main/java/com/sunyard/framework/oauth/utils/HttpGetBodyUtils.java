package com.sunyard.framework.oauth.utils;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author P-JWei
 * @date 2023/7/20 14:55:15
 * @title
 * @description
 */
public class HttpGetBodyUtils {

    /**
     * 获取请求Body
     *
     * @param request 请求头
     * @return Result
     */
    public static String getBodyString(HttpEntityEnclosingRequestBase request) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = request.getEntity().getContent();
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return sb.toString();
    }
}
