package com.sunyard.ecm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;

/**
 * @author 饶昌妹
 * @Date: 2023/8/28
 * @Desc 网管不鉴权接口配置
 */
@Data
@Component
@ConfigurationProperties("gateway.ignore")
public class NotAuthUrlPropertiesConfig {

    private LinkedHashSet<String> shouldSkipUrls;
}
