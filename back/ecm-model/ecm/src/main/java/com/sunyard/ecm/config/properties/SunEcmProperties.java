package com.sunyard.ecm.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author yzy
 * @desc
 * @since 2025/11/19
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties("sunecm")
public class SunEcmProperties {

    @Value("${sunecm.filedownload.path:/home/temp}")
    private String fileDownloadPath;
    @Value("${sunecm.maxSplitFileSize:5242880}")
    private Integer maxSplitFileSize;
    @Value("${sunecm.maxMergeFileSize:10}")
    private Integer maxMergeFileSize;
    @Value("${sunecm.checkDetectionUrl:http://172.1.1.211:9200/reShoot}")
    private String checkDetectionUrl;
    @Value("${sunecm.checkRegularizeUrl:http://172.1.1.211:9202/classRectifyBase64}")
    private String checkRegularizeUrl;
    @Value("${sunecm.checkFuzzyUrl:http://172.1.1.211:9203/classDim}")
    private String checkFuzzyUrl;
    @Value("${sunecm.autoClassUrl:http://172.1.1.211:9209/image-classification}")
    private String autoClassUrl;
    @Value("${sunecm.autoClassDocUrl:http://172.1.4.229:9205/predict}")
    private String autoClassDocUrl;
    @Value("${sunecm.checkReflective:http://172.1.4.229:9208/check_reflective}")
    private String checkReflectiveUrl;
    @Value("${sunecm.checkMissCorner:http://172.1.4.229:9207/check_misscorner}")
    private String checkMissCornerUrl;
    @Value("${sunecm.checkGroupTimeOut:2}")
    private Integer checkGroupTimeOut;

}
