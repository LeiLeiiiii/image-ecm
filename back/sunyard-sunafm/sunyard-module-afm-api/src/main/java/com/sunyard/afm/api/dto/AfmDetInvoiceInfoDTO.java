package com.sunyard.afm.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 在线检测-图像查重结果返参
 * @author P-JWei
 * @date 2024/3/11 14:48:33
 * @title
 * @description
 */
@Data
public class AfmDetInvoiceInfoDTO implements Serializable {

    /**
     * 文件id
     */
    private Long exifId;

    /**
     * 发票代码
     */
    private String invoiceCode;

    /**
     * 发票号
     */
    private String invoiceNum;

    /**
     * 发票校验码
     */
    private String invoiceCheckCode;

    /**
     * 发票日期
     */
    private String invoiceDate;

    /**
     * 发票类型
     */
    private String invoiceType;

    /**
     * 发票金额
     */
    private String invoiceTotal;

    /**
     * token
     */
    private String fileToken;

}
