package com.sunyard.framework.onlyoffice.dto.documentfunction;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @Author 朱山成
 * @time 2023/9/6 14:26
 **/
@Data
@Configuration
public class FwAnonymous implements Serializable {
    @Value("${onlyoffice.editor.customization.anonymous.request:true}")
    private Boolean request;
    @Value("${onlyoffice.editor.customization.anonymous.label:Guest}")
    private String label;
}
