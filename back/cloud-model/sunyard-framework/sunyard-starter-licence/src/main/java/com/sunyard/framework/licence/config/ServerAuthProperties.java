package com.sunyard.framework.licence.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@RefreshScope
public class ServerAuthProperties {

    @Value("${spring.application.name}")
    private  String appName;
    @Value("${server-auth.auth-lic:DefaultValue}")
    private  String serverLIC;
    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private  String nacosUrl;
    @Value("${spring.cloud.nacos.discovery.namespace}")
    private  String nacosNamespace;

    private String privateKey = "d9a4177a02ab23d9914384b10b21c9060870021fa43d9d9dbb3a9a6524929f5f";

}
