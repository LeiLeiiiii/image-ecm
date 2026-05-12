package com.sunyard.ecm.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@RefreshScope
@Component
@ConfigurationProperties("retry")
public class EcmRetryTaskProperties {

    @Value("${retry.max-retry-count:5}")
    private String maxRetryCount;
    @Value("${retry.limit-count:50}")
    private String limitCount;
}
