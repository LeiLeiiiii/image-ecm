package com.sunyard.framework.ocr.dto.invoicevalidation.glority;

import lombok.Data;

import java.util.Map;

/**
 *
 * @author PJW*/
@Data
public class GlorityInvoiceValidationIdentifyResultsDTO {
	private GlorityInvoiceValidationValidationDTO validation;
	private Map<String,Object> details;


}
