package com.sunyard.framework.onlyoffice.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * 配置信息
 * @author PJW
 */
@Data
@Configuration
public class FwAddressUrlConfig {

    @Value("${onlyoffice.localhost-address:}")
    private String localhostAddress;

    /**
     * 回调地址 不包含http表示本机地址
     */
    @Value("${onlyoffice.call-back-url:}")
    private String callBackUrl;

    /**
     * only office服务路径
     * 必须存在
     */
    @Value("${onlyoffice.doc-service:}")
    private String docService;
    /**
     * 下载文件请求接口
     */
    private String downloadFile;

    /**
     * 超时时间
     */
    @Value("${onlyoffice.timeout:3000}")
    private Integer timeout;


    /**
     * 历史文件数量
     */
    @Value("${onlyoffice.hist-num:}")
    private Integer histNum;

    /**
     * JWT令牌
     */
    @Value("${onlyoffice.secret:}")
    private String secret;


    /**
     * 打开文件最大MB 默认 20mb
     */
    @Value("${onlyoffice.maxSize:20971520}")
    private Long maxSize;


    public void setMaxSize(Long maxSize) {
        if (null == maxSize) {
            this.maxSize = Long.valueOf(20 * 1024 * 1024);
        } else {
            this.maxSize = maxSize * 1024 * 1024;
        }
    }
}
