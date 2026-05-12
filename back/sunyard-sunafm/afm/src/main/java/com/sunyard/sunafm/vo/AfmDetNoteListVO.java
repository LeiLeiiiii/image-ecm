package com.sunyard.sunafm.vo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 检测记录入参
 * @author P-JWei
 * @date 2024/3/8 10:39:43
 * @title
 * @description
 */
@Data
public class AfmDetNoteListVO implements Serializable {

    /**
     * 来源系统
     */
    private String sourceSys;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 主索引
     */
    private String businessIndex;

    /**
     * 资料类型
     */
    private String materialType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String fileUrl;

    /**
     * 上传人
     */
    private String uploadUserName;

    /**
     * 查重时间、检测时间（左区间）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date toDetTime;

    /**
     * 查重时间、检测时间（右区间）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date doDetTime;

    /**
     * 查重结果、检测结果
     */
    private Integer detResult;

    /**
     * 导出所用
     */
    private List<Long> noteIds;

    /**
     * 导出所用
     */
    private List<Long> exifIds;

    /**
     * 查重记录类型
     */
    private Integer dupType;
}
