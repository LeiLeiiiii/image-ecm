package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 检测记录-详情返参
 *
 * @author P-JWei
 * @date 2024/3/8 13:43:38
 * @title
 * @description
 */
@Data
public class AfmDetNoteDetailsDTO implements Serializable {

    /**
     * 篡改文件id、发票文件id
     */
    private Long exifId;

    /**
     * 篡改文件base64、发票文件base64
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
     * 文件格式
     */
    private String fileFormat;

    /**
     * 文件大小
     */
    private String fileSize;

    /**
     * 上传机构
     */
    private String uploadOrg;

    /**
     * 上传人
     */
    private String uploadUserName;

    /**
     * 上传时间
     */
    private Date createTime;

    /**
     * 相机型号
     */
    private String cameraModel;

    /**
     * 拍摄时间
     */
    private Date captureTime;

    /**
     * 创建软件
     */
    private String createSoftware;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 海拔
     */
    private String altitude;
}
