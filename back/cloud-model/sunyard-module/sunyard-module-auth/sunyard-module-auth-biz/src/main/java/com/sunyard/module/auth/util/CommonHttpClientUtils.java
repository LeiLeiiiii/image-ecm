package com.sunyard.module.auth.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * @author P-JWei
 * @date 2024/3/22 17:51:02
 * @title
 * @description
 */
public class CommonHttpClientUtils {

    private static final PoolingHttpClientConnectionManager CONNECTION_MANAGER;

    private static RequestConfig requestConfig;

    /**
     * 最大总连接数
     */
    private static final int MAX_TOTAL_CONNECTIONS = 50;
    /**
     * 每个路由的最大连接数
     */
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10;
    /**
     * 连接超时时间（毫秒）
     */
    private static final int CONNECT_TIMEOUT = 100000;
    /**
     * 读取超时时间（毫秒）
     */
    private static final int SOCKET_TIMEOUT = 100000;

    private CommonHttpClientUtils() {
    }

    static {
        // 创建套接字工厂注册中心
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                // 注册普通HTTP连接套接字工厂
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                // 注册HTTPS连接套接字工厂
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        CONNECTION_MANAGER = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // 设置最大总连接数
        CONNECTION_MANAGER.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        // 设置每个路由的默认最大连接数
        CONNECTION_MANAGER.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        // 创建请求配置
        requestConfig = RequestConfig.custom()
                // 设置连接超时时间
                .setConnectTimeout(CONNECT_TIMEOUT)
                // 设置读取超时时间
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
    }

    /**
     * 创建带有连接池的HttpClient实例
     *
     * @return Result CloseableHttpClient 带有连接池的HttpClient实例
     * @throws NoSuchAlgorithmException 如果找不到指定的算法
     * @throws KeyManagementException   如果密钥管理发生错误
     */
    public static CloseableHttpClient createHttpClient() throws NoSuchAlgorithmException {
        //创建默认 SSLContext
        SSLContext sslContext = SSLContext.getDefault();

        // 指定使用 TLSv1.2 协议
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{"TLSv1.2"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier()
        );
        return HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                // 设置连接管理器
                .setConnectionManager(CONNECTION_MANAGER)
                // 设置默认请求配置
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}
