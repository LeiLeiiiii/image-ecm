package com.sunyard.framework.onlyoffice.dto.edit;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @Author 朱山成
 * @time 2023/9/6 17:18
 **/
@Data
@Configuration
public class FwReview implements Serializable {
    @Value("${onlyoffice.editor.customization.review.hideReviewDisplay:false}")
    private Boolean hideReviewDisplay;
    @Value("${onlyoffice.editor.customization.review.hoverMode:false}")
    private Boolean hoverMode;
}
