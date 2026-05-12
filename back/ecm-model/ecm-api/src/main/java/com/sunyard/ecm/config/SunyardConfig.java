package com.sunyard.ecm.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author pjw
 * @Description 配置文件读取映射配置类
 * @since 2024/2/22 10:56
 */
@Data
@Configuration
public class SunyardConfig {

    //运行的类型：local本地，remote
    @Value("${sunyard.appId}")
    private String appId;
    @Value("${sunyard.version}")
    private String version;


    @Value("${sunyard.appSecret}")
    private String appSecret;

    @Value("${sunyard.privateKey}")
    private String privateKey;

    @Value("${sunyard.baseUrl}")
    private String baseUrl;

    @Value("${sunyard.referer}")
    private String referer;

}
