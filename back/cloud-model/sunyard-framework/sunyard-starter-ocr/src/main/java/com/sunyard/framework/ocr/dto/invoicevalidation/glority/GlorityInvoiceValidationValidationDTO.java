package com.sunyard.framework.ocr.dto.invoicevalidation.glority;

import lombok.Data;

/**
 *
 * @author PJW*/
@Data
public class GlorityInvoiceValidationValidationDTO {
    private Integer code;
    private String message;
    private String asyncToken;

}
