package com.sunyard.framework.ocr.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 验真
 *
 * @author
 */
@Data
public class InvoiceValidation implements Serializable {

    private static final long serialVersionUID = -7047660668620206082L;
    /**
     * 主键
     */
    private String validationId;
    /**
     * 几个入参拼接后再md5生产的字符串,用于辅助查询
     */
    private String paramMd5;
    /**
     * 发票号码
     */
    private String invoiceNumber;
    /**
     * 发票代码
     */
    private String invoiceCode;
    /**
     * 发票类型代码
     */
    private String invoiceType;
    /**
     * 校验结果 通过:1  不通过:0
     */
    private Integer checkResult;
    /**
     * 校验码后6位
     */
    private String checkCode;
    /**
     * 专票(税前金额)、机动车票(税前金额)、二手车票(总价)不为空，普票可为空
     */
    private String pretaxAmount;
    /**
     * 开票日期
     */
    private String invoiceDate;
    /**
     * 验真接口返回数据序列化字符串
     */
    private String validateData;
    /**
     * 结果描述
     */
    private String resultDes;
}
