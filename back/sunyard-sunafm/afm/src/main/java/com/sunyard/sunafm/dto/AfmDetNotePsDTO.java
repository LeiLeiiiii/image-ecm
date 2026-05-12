package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author P-JWei
 * @date 2024/4/7 14:42:46
 * @title
 * @description
 */
@Data
public class AfmDetNotePsDTO implements Serializable {

    private Integer psCount;

    private String fileUrl;
}
