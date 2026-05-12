package com.sunyard.framework.ocr.dto.invoicevalidation.glority;

import lombok.Data;

/**
 *
 * @author PJW*/
@Data
public class GlorityInvoiceValidationResultDTO {
    /**
     * 返回转态码1代表验真成功，0代表验真失败
     */
    private Integer result;
    /**
     *
     */
    private GlorityInvoiceValidationResponseDTO response;
    /**
     * 错误代码
     */
    private Integer error;
    /**
     * 错误描述
     */
    private String message;

    /**
     * */
    public int getResultCode() {
        if (!new Integer("1").equals(result)
                || response == null
                || response.getData() == null
                || !new Integer("1").equals(response.getData().getResult())
            || response.getData().getIdentifyResults() == null || response.getData().getIdentifyResults().isEmpty()
            || response.getData().getIdentifyResults().get(0).getValidation() == null) {
            return -1;
        } else {
            Integer resultCode = response.getData().getIdentifyResults().get(0).getValidation().getCode();
            return resultCode;
        }
    }

}
