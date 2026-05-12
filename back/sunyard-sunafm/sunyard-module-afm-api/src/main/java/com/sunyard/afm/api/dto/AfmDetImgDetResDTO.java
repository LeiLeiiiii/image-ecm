package com.sunyard.afm.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 在线检测-图像查重结果返参
 * @author P-JWei
 * @date 2024/3/11 14:48:33
 * @title
 * @description
 */
@Data
public class AfmDetImgDetResDTO implements Serializable {

    /**
     * 文件md5
     */
    private String fileIndex;

    /**
     * 文件url
     */
    private String fileUrl;

    /**
     * 来源系统
     */
    private String sourceSys;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务索引（主索引）
     */
    private String businessIndex;

    /**
     * 资料类型
     */
    private String materialType;

    /**
     * 上传人登录名
     */
    private String uploadUserCode;

    /**
     * 上传人（姓名）
     */
    private String uploadUserName;

    /**
     * 上传机构
     */
    private String uploadOrg;

    /**
     * 文件元数据（json格式）
     */
    private String fileExif;

    /**
     * 文件查询条件
     */
    private String queryExpr;

    /**
     * 查重结果回调地址
     */
    private String backUrl;

    /**
     * 检测方式
     */
    private String isInvoice;

    /**
     * 查重结果
     */
    private Map detRes;
    /**
     * 防篡改结果
     */
    private Map invoiceRes;
    /**
     * 验证结果
     */
    private Map psRes;


}
