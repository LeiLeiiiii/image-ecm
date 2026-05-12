package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author P-JWei
 * @date 2024/4/3 14:54:43
 * @title
 * @description
 */
@Data
public class AfmDetOnlinePsDetDTO implements Serializable {

    /**
     * 源文件base64
     */
    private String sourceFileBase64;

    /**
     * 篡改处
     */
    private Integer psCount;

    /**
     * 含篡改信息的base64
     */
    private String psFileBase64;
}
