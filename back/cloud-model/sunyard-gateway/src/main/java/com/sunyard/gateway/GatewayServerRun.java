package com.sunyard.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.sunyard.gateway.util.ApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhouleibin
 * @Type com.sunyard.am
 * @Desc
 * @date 2021/6/28 16:16
 */
@Slf4j
@SpringBootApplication
public class GatewayServerRun {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GatewayServerRun.class, args);
        ApplicationContextUtils.setApplicationContext(run);
        log.info("Tomcat started on port(s): 8080 (http) with context path ''");
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/6/28 zhouleibin creat
 */
