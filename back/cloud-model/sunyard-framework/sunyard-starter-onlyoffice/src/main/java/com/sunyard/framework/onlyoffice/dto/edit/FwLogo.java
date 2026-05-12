package com.sunyard.framework.onlyoffice.dto.edit;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @Author 朱山成
 * @time 2023/9/6 17:10
 **/
@Data
@Configuration
public class FwLogo implements Serializable {
    @Value("${onlyoffice.editor.customization.logo.image:}")
    private String image;
    @Value("${onlyoffice.editor.customization.logo.imageDark:}")
    private String imageDark;
    @Value("${onlyoffice.editor.customization.logo.url:}")
    private String url;
}
