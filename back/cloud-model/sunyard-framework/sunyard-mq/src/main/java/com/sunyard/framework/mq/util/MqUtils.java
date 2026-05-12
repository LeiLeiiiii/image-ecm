package com.sunyard.framework.mq.util;


import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author: Ant
 * @Date: 2018/11/23 13:28
 * @Description:
 */
public class MqUtils {
    // todo mq为什么 不是用客户端？
    /**
     * 获取api消息
     * @param host host
     * @param port 端口
     * @param username 账号
     * @param password 密码
     * @return String
     */
//    public static String getApiMessage(String host,String port,String username,String password) {
//        //发送一个GET请求
//        HttpURLConnection httpConn = null;
//        BufferedReader in = null;
//
//        String urlString = "http://" + host + ":" + port + "/api/queues/";
//        try {
//            URL url = new URL(urlString);
//            httpConn = (HttpURLConnection) url.openConnection();
//            //设置用户名密码
//            String auth = username + ":" + password;
//            BASE64Encoder enc = new BASE64Encoder();
//            String encoding = enc.encode(auth.getBytes(StandardCharsets.UTF_8));
//            httpConn.setDoOutput(true);
//            httpConn.setRequestProperty("Authorization", "Basic " + encoding);
//            //读取响应
//
//            // 建立实际的连接
//            httpConn.connect();
//            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                StringBuilder content = new StringBuilder();
//                String tempStr = "";
//                in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(),StandardCharsets.UTF_8));
//                while ((tempStr = in.readLine()) != null) {
//                    content.append(tempStr);
//                }
//                in.close();
//                httpConn.disconnect();
//                return content.toString();
//            } else {
//                httpConn.disconnect();
//                return "";
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 10_000;
    private static final int MAX_RESPONSE_SIZE = 10_000_000; // 10MB最大响应限制

    public static String getApiMessage(String host, String port, char[] username, char[] password) {
        if (host == null || port == null || username == null || password == null) {
            throw new IllegalArgumentException("参数不能为null");
        }

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL("http://" + host + ":" + port + "/api/queues/");

            // 2. 创建安全连接
            connection = (HttpURLConnection) url.openConnection();
            configureSecureConnection(connection, username, password);

            // 3. 处理响应
            return processResponse(connection);
        } catch (IOException e) {
            throw new SecurityException("API请求失败", e);
        } finally {
            // 4. 安全清理资源
            cleanup(connection, reader, password);
        }
    }

    private static void configureSecureConnection(HttpURLConnection conn,
                                                  char[] username, char[] password) throws IOException {
        // 设置超时防止拒绝服务攻击
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);

        // 安全构建认证头
        String auth = buildAuthHeader(username, password);
        conn.setRequestProperty("Authorization", "Basic " + auth);

        // 其他安全设置
        conn.setRequestMethod("GET");
        conn.setDoOutput(false); // GET请求不需要输出流
        conn.setRequestProperty("Accept-Charset", "UTF-8");
    }

    private static String buildAuthHeader(char[] username, char[] password) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            // 转换 username 字符数组为 UTF-8 字节数组并写入
            byte[] usernameBytes = new String(username).getBytes(StandardCharsets.UTF_8);
            bos.write(usernameBytes);

            // 写入分隔符冒号
            bos.write(':');

            // 转换 password 字符数组为 UTF-8 字节数组并写入
            byte[] passwordBytes = new String(password).getBytes(StandardCharsets.UTF_8);
            bos.write(passwordBytes);

            // 获取完整的认证字节并进行 Base64 编码
            byte[] authBytes = bos.toByteArray();
            return Base64.getEncoder().encodeToString(authBytes);
        } catch (IOException e) {
            // 这里根据实际场景处理异常，比如抛自定义异常或返回默认值
            throw new RuntimeException("构建认证头失败", e);
        } finally {
            // 清除敏感数据
            Arrays.fill(username, '\0');
            Arrays.fill(password, '\0');
        }
    }


    private static String processResponse(HttpURLConnection conn) throws IOException {
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpsURLConnection.HTTP_OK) {
            throw new SecurityException("API请求失败，状态码: " + responseCode);
        }

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder response = new StringBuilder();
            char[] buffer = new char[8192];
            int bytesRead;
            int totalBytes = 0;

            while ((bytesRead = in.read(buffer)) != -1) {
                totalBytes += bytesRead;
                if (totalBytes > MAX_RESPONSE_SIZE) {
                    throw new SecurityException("响应数据超过最大限制");
                }
                response.append(buffer, 0, bytesRead);
            }
            return response.toString();
        }
    }

    private static void cleanup(HttpURLConnection conn, BufferedReader reader, char[] password) {
        // 确保密码被清除
        if (password != null) {
            Arrays.fill(password, '\0');
        }

        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            // 记录日志
        }

        if (conn != null) {
            conn.disconnect();
        }
    }
}



