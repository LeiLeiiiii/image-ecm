package com.sunyard.sunafm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author P-JWei
 * @date 2024/4/8 16:28:53
 * @title
 * @description
 */
@Getter
@AllArgsConstructor
public enum InvoiceDetResultEnum {
    INVOICE_VERIFY(1, "验真不通过"),
    INVOICE_DUP(2, "发票号重复"),
    INVOICE_LINK(3, "发票号连续");
    private Integer code;

    private String desc;

    public static String getDescByCode(Integer code) {
        for (InvoiceDetResultEnum item : InvoiceDetResultEnum.values()) {
            if (item.getCode().equals(code)) {
                return item.getDesc();
            }
        }
        return "";
    }
}
