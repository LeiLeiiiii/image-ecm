package com.sunyard.gateway.config;

import java.util.LinkedHashSet;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @author zhouleibin
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties("gateway.auth")
public class WhiteListProperties {

    private LinkedHashSet<String> whiteList;

    private LinkedHashSet<String> apiWhiteList;
}
