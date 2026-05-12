package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 检测记录-详情-文件详情返参
 *
 * @author P-JWei
 * @date 2024/3/8 13:46:28
 * @title
 * @description
 */
@Data
public class AfmDetNoteResultDetailsDTO implements Serializable {

    /**
     * 文件id（用于获取文件缩略图/原图）
     */
    private Long exifId;

    /**
     * 文件base64
     */
    private String fileBase64;

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
     * 上传人
     */
    private String uploadUserName;

    /**
     * 上传时间
     */
    private Date createTime;

    /**
     * 相似度
     */
    private String similarity;
}
