package com.sunyard.framework.ocr.dto.invoicevalidation;

import com.sunyard.framework.ocr.entity.InvoiceValidation;
import lombok.Data;

import java.util.List;

/**
 * 请求参数类
 * @author PJW
 */
@Data
public class ValidateRequestParamDTO {
    private String code;
    private String number;
    private String checkCode;
    private String pretaxAmount;
    private String date;
    private String type;
    private String total;
    private List<InvoiceValidation> invoiceValidationList;
}
