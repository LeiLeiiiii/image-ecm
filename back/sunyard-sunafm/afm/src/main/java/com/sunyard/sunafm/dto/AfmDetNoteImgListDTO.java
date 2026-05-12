package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 检测记录-图像查重列表返回参
 * @author P-JWei
 * @date 2024/3/11 16:31:01
 * @title
 * @description
 */
@Data
public class AfmDetNoteImgListDTO implements Serializable {
    /**
     * 来源系统
     */
    private Long noteId;
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
     * 文件id
     */
    private String exifIdOrMd5;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 上传人
     */
    private String uploadUser;

    /**
     * 查重时间
     */
    private Date imgDupTime;

    /**
     * 查重结果（字典：0、1）
     */
    private Double imgDupResult;

    /**
     * 查重结果（字典值：正常、疑似重复）
     */
    private String imgDupResultStr;


    /**
     * 查重结果（字典值：正常、疑似重复）
     */
    private Integer isRepeat;
}
