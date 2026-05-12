package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 在线检测-已传文件列表返参
 * @author P-JWei
 * @date 2024/3/11 14:45:00
 * @title
 * @description
 */
@Data
public class AfmDetOnlineFileDTO implements Serializable {
    /**
     * 主键
     */
    private String fileIndex;
    /**
     * 主键
     */
    private Long exifId;

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
    private Date uploadTime;
}
