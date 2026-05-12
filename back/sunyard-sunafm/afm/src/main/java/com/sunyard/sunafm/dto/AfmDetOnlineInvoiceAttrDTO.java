package com.sunyard.sunafm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 在线检测-发票检测-发票属性返回参
 * @author P-JWei
 * @date 2024/3/11 17:03:21
 * @title
 * @description
 */
@Data
public class AfmDetOnlineInvoiceAttrDTO implements Serializable {

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

}
