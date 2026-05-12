package com.sunyard.sunafm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author P-JWei
 * @date 2024/3/27 17:16:57
 * @title
 * @description
 */
@Getter
@AllArgsConstructor
public enum InvoiceValidateEnum {

    PROFESSIONAL_INVOICE("10100", "增值税电子专用发票"),
    PROFESSIONAL_INVOICE_NO_ELE("10100", "增值税专用发票"),
    ORDINARY_INVOICE("10101", "增值税电子普通发票"),
    ORDINARY_INVOICE_NO_ELE("10101", "增值税普通发票");

    private String code;

    private String desc;

    public static String getCodeByDesc(String desc) {
        for (InvoiceValidateEnum item : InvoiceValidateEnum.values()) {
            if (item.getDesc().equals(desc)) {
                return item.getCode();
            }
        }
        return "";
    }
}
