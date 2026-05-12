package com.sunyard.module.system.config;

import java.security.cert.X509Certificate;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @Type RestTemplateConfig.java
 * @Desc
 * @author zhouleibin
 * @date 2019/5/29 17:34
 * @version
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建https restemplate bean
     * @param httpsFactory
     * @return
     */
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory httpsFactory) {
        return new RestTemplate(httpsFactory);
    }

    /**
     * 创建http客户端工厂
     */
    @Bean(name = "httpsFactory")
    public ClientHttpRequestFactory clientHttpRequestFactory() throws Exception {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        //设置过期时间
        factory.setReadTimeout(180000);
        factory.setConnectTimeout(180000);
        // https
        SSLContextBuilder builder = new SSLContextBuilder();
        //全部信任 不做身份鉴定
        builder.loadTrustMaterial(null, (X509Certificate[] x509Certificates, String s) -> true);
        //客户端支持SSLv2Hello，SSLv3,TLSv1，TLSv1
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(builder.build(),
                new String[] { "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2" }, null,
                NoopHostnameVerifier.INSTANCE);
        //为自定义连接器注册http与https
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory> create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", socketFactory).build();
        PoolingHttpClientConnectionManager phccm = new PoolingHttpClientConnectionManager(registry);
        phccm.setMaxTotal(200);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory)
                .setConnectionManager(phccm).setConnectionManagerShared(true).build();
        factory.setHttpClient(httpClient);

        return factory;
    }
}

/**
 * Revision history
 * -------------------------------------------------------------------------
 *
 * Date Author Note
 * -------------------------------------------------------------------------
 * 2019/5/29 zhouleibin create
 */
