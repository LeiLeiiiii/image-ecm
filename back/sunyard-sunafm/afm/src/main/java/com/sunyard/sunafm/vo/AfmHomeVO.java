package com.sunyard.sunafm.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 首页入参
 *
 * @author P-JWei
 * @date 2024/3/7 16:16:36
 * @title
 * @description
 */
@Data
public class AfmHomeVO implements Serializable {

    /**
     * 左区间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date toTime;

    /**
     * 右区间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date doTime;

    /**
     * 结果排序（相似度、篡改处）
     */
    private Integer resultSort;

    /**
     * 时间排序
     */
    private Integer timeSort;
}
