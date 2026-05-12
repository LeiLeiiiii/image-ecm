package com.sunyard.mytool.entity.es.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.easyes.annotation.HighLight;

import java.io.Serializable;
import java.util.Date;


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
