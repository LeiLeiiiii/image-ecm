package com.sunyard.framework.ocr.dto.invoicevalidation.glority;

import lombok.Data;

/**
 *
 * @author PJW*/
@Data
public class GlorityInvoiceValidationItemsDTO {
	private String name;
	private String specification;
	private String unit;
	private String quantity;
	private String price;
	private String total;
    private String taxRate;
	private String tax;

}
