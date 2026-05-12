package com.sunyard.framework.onlyoffice.dto.edit;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @Author 朱山成
 * @time 2023/9/6 17:04
 **/
@Data
@Configuration
public class FwGoback implements Serializable {
    @Value("${onlyoffice.editor.customization.goback.blank:true}")
    private Boolean blank;
    @Value("${onlyoffice.editor.customization.goback.blank:false}")
    private Boolean requestClose;
    @Value("${onlyoffice.editor.customization.goback.text:}")
    private String text;
    @Value("${onlyoffice.editor.customization.goback.url:}")
    private String url;
}
