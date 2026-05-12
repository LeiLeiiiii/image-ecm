package com.sunyard.sunafm.dto;


import lombok.Data;

import java.io.Serializable;

/**
 * @author PJW
 */
@Data
public class ValidateRequestParamDTO implements Serializable {

    /**
     * 发票代码
     */
    private String code;

    /**
     * 发票号码
     */
    private String number;

    /**
     * 校验码
     */
    private String check_code;

    /**
     * 金额(部分票据使用此名称字段)
     */
    private String pretax_amount;

    /**
     * 日期
     */
    private String date;

    /**
     * 类型
     */
    private String type;

    /**
     * 金额(部分票据使用此名称字段)
     */
    private String total;
}
