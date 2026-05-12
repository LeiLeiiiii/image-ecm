package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 首页-分布图返参
 * @author P-JWei
 * @date 2024/3/7 16:35:31
 * @title
 * @description
 */
@Data
public class AfmHomeProfileDTO implements Serializable {

    /**
     * 分布图数据
     */
    private List<Map<String,Object>> profileData;
}
