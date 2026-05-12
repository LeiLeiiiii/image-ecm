package com.sunyard.framework.onlyoffice.dto.edit;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @Author 朱山成
 * @time 2023/9/6 16:58
 **/
@Data
@Configuration
public class FwFeedback implements Serializable {
    @Value("${onlyoffice.editor.customization.feedback.visible:false}")
    private Boolean visible;
    @Value("${onlyoffice.editor.customization.feedback.url:}")
    private String url;
}
