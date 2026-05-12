package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 首页table返参
 *
 * @author P-JWei
 * @date 2024/3/7 16:31:17
 * @title
 * @description
 */
@Data
public class AfmHomeListDTO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 文件id或者MD5
     *
     */
    private String exifIdOrMd5;

    /**
     * 来源系统
     */
    private String sourceSys;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 检测结果、相似度
     */
    private String detResult;

    /**
     * 检测时间
     */
    private Date detTime;
}
