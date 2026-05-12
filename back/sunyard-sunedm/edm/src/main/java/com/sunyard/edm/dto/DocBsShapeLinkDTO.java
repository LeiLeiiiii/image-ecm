package com.sunyard.edm.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author PJW 2022/12/13 17:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DocBsShapeLinkDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * 外部链接 仅用于‘外部分享’
	 */
    private String url;

    /**
	 * 访问密码 仅用于‘外部分享’
	 */
    private String pwd;

}
