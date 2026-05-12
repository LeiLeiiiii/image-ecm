package com.sunyard.framework.elasticsearch.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.easyes.annotation.HighLight;

import java.io.Serializable;
import java.util.Date;

/**
 * @author P-JWei
 * @date 2023/10/20 10:25:04
 * @title
 * @description
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseAttachment implements Serializable {

    private Date date;

    private String contentType;

    private String author;

    private String language;

    @HighLight(preTag = "<span style='color:red'>", postTag = "</span>",fragmentSize = 100,numberOfFragments = 5)
    private String content;

    private Integer contentLength;
}
