package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 首页总条数返参
 *
 * @author P-JWei
 * @date 2024/3/7 16:24:29
 * @title
 * @description
 */
@Data
public class ArmHomeCountDTO implements Serializable {

    /**
     * 总数
     */
    private Long count;

    /**
     * 异常条数
     */
    private Long abnormalCount;

    /**
     * 每日条数
     */
    private List<Map<String,String>> detailsList;

}
